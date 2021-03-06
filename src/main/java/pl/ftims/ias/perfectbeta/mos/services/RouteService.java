package pl.ftims.ias.perfectbeta.mos.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.PhotoDTO;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.RatingDTO;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.RouteDTO;
import pl.ftims.ias.perfectbeta.entities.*;
import pl.ftims.ias.perfectbeta.entities.enums.GymStatusEnum;
import pl.ftims.ias.perfectbeta.exceptions.*;
import pl.ftims.ias.perfectbeta.mos.repositories.ClimbingGymRepository;
import pl.ftims.ias.perfectbeta.mos.repositories.RatingRepository;
import pl.ftims.ias.perfectbeta.mos.repositories.RouteRepository;
import pl.ftims.ias.perfectbeta.mos.repositories.UserMosRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(transactionManager = "mosTransactionManager", isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
public class RouteService implements RouteServiceLocal {

    RouteRepository routeRepository;
    UserMosRepository userMosRepository;
    ClimbingGymRepository climbingGymRepository;
    RatingRepository ratingRepository;


    @Autowired
    public RouteService(RouteRepository routeRepository, ClimbingGymRepository climbingGymRepository, UserMosRepository userMosRepository, RatingRepository ratingRepository) {
        this.routeRepository = routeRepository;
        this.climbingGymRepository = climbingGymRepository;
        this.userMosRepository = userMosRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Page<RouteEntity> findAllByGymId(Long gymId, Pageable page) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(gymId)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(gymId));

        return routeRepository.findAllByClimbingGym(gym, page);
    }

    @Override
    public RouteEntity addRoute(RouteDTO routeDTO) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(routeDTO.getClimbingGymId())
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(routeDTO.getClimbingGymId()));

        if (Boolean.FALSE.equals(checkIfPermitted(gym))) {
            throw NotAllowedAppException.createYouAreNotMaintainerOrOwnerException();
        }
        if (!gym.getStatus().equals(GymStatusEnum.VERIFIED)) {
            throw NotAllowedAppException.createGymNotVerifiedException();
        }


        RouteEntity route = new RouteEntity(routeDTO.getRouteName(), routeDTO.getHoldsDetails(), routeDTO.getDescription(), routeDTO.getDifficulty(), 0.0, gym, new ArrayList<PhotoEntity>());
        route = routeRepository.save(route);
        if (routeDTO.getPhotos().size() != 0) {
            List<PhotoEntity> photoEntityList = new ArrayList<>();
            for (PhotoDTO photo : routeDTO.getPhotos()) {
                photoEntityList.add(new PhotoEntity(photo.getPhotoUrl(), route));

            }
            route.setPhotos(photoEntityList);
            routeRepository.save(route);
        }


        return route;
    }

    @Override
    public ResponseEntity removeRoute(Long gymId, Long routeId) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(gymId)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(gymId));

        if (Boolean.FALSE.equals(checkIfPermitted(gym))) {
            throw NotAllowedAppException.createYouAreNotMaintainerOrOwnerException();
        }
        if (!gym.getStatus().equals(GymStatusEnum.VERIFIED)) {
            throw NotAllowedAppException.createGymNotVerifiedException();
        }
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(routeId));

        routeRepository.delete(route);
        return ResponseEntity.ok().build();
    }

    @Override
    public RouteEntity editRouteDetails(Long gymId, Long routeId, RouteDTO routeDTO) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(gymId)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(gymId));

        if (Boolean.FALSE.equals(checkIfPermitted(gym))) {
            throw NotAllowedAppException.createYouAreNotMaintainerOrOwnerException();
        }
        if (!gym.getStatus().equals(GymStatusEnum.VERIFIED)) {
            throw NotAllowedAppException.createGymNotVerifiedException();
        }

        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(routeId));
        route.setRouteName(routeDTO.getRouteName());
        route.setDifficulty(routeDTO.getDifficulty());
        return route;
    }

    @Override
    public RouteEntity addRouteToFavourites(Long id) throws AbstractAppException {
        RouteEntity route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userMosRepository.findByLogin(auth.getName())
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));

        if (route.getLikedBy().contains(user)) {
            throw UniqueConstraintAppException.createFavouriteAlreadyException();
        }
        route.getLikedBy().add(user);

        return routeRepository.save(route);
    }

    @Override
    public ResponseEntity deleteRouteFromFavourites(Long id) throws AbstractAppException {
        RouteEntity route = routeRepository.findById(id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userMosRepository.findByLogin(auth.getName())
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));

        if (!route.getLikedBy().contains(user)) {
            throw UniqueConstraintAppException.createNotFavouriteAlreadyException();
        }
        route.getLikedBy().remove(user);

        routeRepository.save(route);
        return ResponseEntity.ok().build();
    }

    @Override
    public Page<RouteEntity> getFavouriteRoutes(Pageable page) throws AbstractAppException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userMosRepository.findByLogin(auth.getName())
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));

        return routeRepository.findAllFavouritesRoutes(user, page);
    }


    @Override
    public RouteEntity updateRating(Long route_id, RatingDTO ratingDTO) throws AbstractAppException {
        RouteEntity route = routeRepository.findById(route_id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(route_id));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userMosRepository.findByLogin(auth.getName())
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));
        route.getRatings().add(new RatingEntity(ratingDTO.getRate(), ratingDTO.getComment(), route, user));

        routeRepository.save(route);
        calculateRatings(route);
        return route;
    }

    @Override
    public ResponseEntity deleteOwnRating(Long rating_id) throws AbstractAppException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userMosRepository.findByLogin(auth.getName())
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));

        RatingEntity rate = ratingRepository.findById(rating_id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(rating_id));
        RouteEntity route = routeRepository.findById(rate.getRoute().getId())
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(rate.getRoute().getId()));
        if (rate.getUser().equals(user)) {
            route.getRatings().remove(rate);
            ratingRepository.delete(rate);
        } else throw NotAllowedAppException.createYouAreNotMaintainerOrOwnerException();

        routeRepository.save(route);
        calculateRatings(route);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity deleteRatingByOwnerOrMaintainer(Long rating_id) throws AbstractAppException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        RatingEntity rate = ratingRepository.findById(rating_id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(rating_id));
        RouteEntity route = routeRepository.findById(rate.getRoute().getId())
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(rate.getRoute().getId()));
        ClimbingGymEntity gym = climbingGymRepository.findById(route.getClimbingGym().getId())
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(route.getClimbingGym().getId()));

        if (checkIfPermitted(gym)) {
            route.getRatings().remove(rate);
            ratingRepository.delete(rate);
        } else throw NotAllowedAppException.createYouAreNotMaintainerOrOwnerException();

        routeRepository.save(route);
        calculateRatings(route);
        return ResponseEntity.ok().build();
    }

    @Override
    public List<RatingEntity> getRatings(Long route_id) throws AbstractAppException {
        RouteEntity route = routeRepository.findById(route_id)
                .orElseThrow(() -> RouteNotFoundException.createRouteWithProvidedIdNotFoundException(route_id));
        return route.getRatings();
    }

    private void calculateRatings(RouteEntity route) {
        Double avgRating = 0.0;
        for (RatingEntity r : route.getRatings()) {
            avgRating += r.getRate();
        }
        route.setAvgRating(avgRating / route.getRatings().size());
        routeRepository.save(route);
    }

    private Boolean checkIfPermitted(ClimbingGymEntity gym) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (gym.getOwner().getLogin().equals(username)) {
            return Boolean.TRUE;
        } else {
            for (GymMaintainerEntity m : gym.getMaintainers()) {
                if (m.getUser().getLogin().equals(username)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

}
