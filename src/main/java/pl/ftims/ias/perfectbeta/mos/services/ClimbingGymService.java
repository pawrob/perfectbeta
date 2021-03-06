package pl.ftims.ias.perfectbeta.mos.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.GymDetailsDTO;
import pl.ftims.ias.perfectbeta.entities.*;
import pl.ftims.ias.perfectbeta.entities.enums.GymStatusEnum;
import pl.ftims.ias.perfectbeta.exceptions.AbstractAppException;
import pl.ftims.ias.perfectbeta.exceptions.GymNotFoundException;
import pl.ftims.ias.perfectbeta.exceptions.NotAllowedAppException;
import pl.ftims.ias.perfectbeta.exceptions.UserNotFoundAppException;
import pl.ftims.ias.perfectbeta.mos.repositories.ClimbingGymRepository;
import pl.ftims.ias.perfectbeta.mos.repositories.GymMaintainerRepository;
import pl.ftims.ias.perfectbeta.mos.repositories.UserMosRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "mosTransactionManager", isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
public class ClimbingGymService implements ClimbingGymServiceLocal {

    ClimbingGymRepository climbingGymRepository;
    UserMosRepository userMosRepository;
    GymMaintainerRepository gymMaintainerRepository;

    @Autowired
    public ClimbingGymService(ClimbingGymRepository climbingGymRepository, UserMosRepository userMosRepository, GymMaintainerRepository gymMaintainerRepository) {
        this.climbingGymRepository = climbingGymRepository;
        this.userMosRepository = userMosRepository;
        this.gymMaintainerRepository = gymMaintainerRepository;
    }


    public Page<ClimbingGymEntity> listOwnedGyms(Pageable page) throws AbstractAppException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity owner = userMosRepository.findByLogin(auth.getName()).orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));
        return climbingGymRepository.findByOwner(owner, page);
    }

    public Page<ClimbingGymEntity> listMaintainedGyms(Pageable page) throws AbstractAppException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity owner = userMosRepository.findByLogin(auth.getName()).orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(auth.getName()));
        List<GymMaintainerEntity> gymMaintainerEntities = gymMaintainerRepository.findByUser(owner);
        List<ClimbingGymEntity> climbingGymEntities = new ArrayList<>();
        for (GymMaintainerEntity g : gymMaintainerEntities) {
            climbingGymEntities.add(g.getMaintainedGym());
        }
        return new PageImpl<ClimbingGymEntity>(climbingGymEntities, page, climbingGymEntities.size());
    }

    public Page<ClimbingGymEntity> listAllGyms(Pageable page) {
        return climbingGymRepository.findAll(page);
    }

    public ClimbingGymEntity findById(Long id) throws AbstractAppException {
        return climbingGymRepository.findById(id).orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(id));
    }

    public ClimbingGymEntity findVerifiedById(Long id) throws AbstractAppException {
        return climbingGymRepository.findVerifiedById(id).orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(id));
    }

    public Page<ClimbingGymEntity> listVerifiedGyms(Pageable page) {
        return climbingGymRepository.findAllVerified(page);
    }

    public ClimbingGymEntity registerNewClimbingGym(String gymName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> owner = userMosRepository.findByLogin(auth.getName());
        ClimbingGymEntity gym = new ClimbingGymEntity(gymName, owner.get());
        gym.setGymDetails(new GymDetailsEntity(gym));

        return climbingGymRepository.save(gym);
    }

    public ClimbingGymEntity verifyGym(Long id) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(id)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(id));

        gym.setStatus(GymStatusEnum.VERIFIED);
        return climbingGymRepository.save(gym);
    }

    public ClimbingGymEntity closeGym(Long id) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(id)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(id));

        gym.setStatus(GymStatusEnum.CLOSED);
        return climbingGymRepository.save(gym);
    }

    public ClimbingGymEntity editGymDetails(Long id, GymDetailsDTO detailsDTO) throws AbstractAppException {

        ClimbingGymEntity gym = climbingGymRepository.findById(id)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(id));
        if (!gym.getOwner().getLogin().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw NotAllowedAppException.createNotAllowedException();
        }
        GymDetailsEntity details = gym.getGymDetails();
        details.setCountry(detailsDTO.getCountry());
        details.setCity(detailsDTO.getCity());
        details.setStreet(detailsDTO.getStreet());
        details.setNumber(detailsDTO.getNumber());
        details.setDescription(detailsDTO.getDescription());
        gym.setGymDetails(details);

        return climbingGymRepository.save(gym);
    }


    public ClimbingGymEntity addMaintainer(Long gymId, String username) throws AbstractAppException {
        ClimbingGymEntity gym = climbingGymRepository.findById(gymId)
                .orElseThrow(() -> GymNotFoundException.createGymWithProvidedIdNotFoundException(gymId));

        if (!gym.getOwner().getLogin().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw NotAllowedAppException.createNotAllowedException();
        }
        UserEntity maintainer = userMosRepository.findByLogin(username)
                .orElseThrow(() -> UserNotFoundAppException.createUserWithProvidedLoginNotFoundException(username));
        if (!checkIfManager(maintainer)) {
            throw NotAllowedAppException.createNotAllowedException();
        }
        gym.getMaintainers().add(new GymMaintainerEntity(gym, maintainer, true));
        return climbingGymRepository.save(gym);
    }

    private boolean checkIfManager(UserEntity user) {
        Collection<AccessLevelEntity> accessLevels = user.getAccessLevels();
        for (AccessLevelEntity accessLevel : accessLevels) {
            if (accessLevel.getAccessLevel().equals("MANAGER") && accessLevel.getActive().equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }


}
