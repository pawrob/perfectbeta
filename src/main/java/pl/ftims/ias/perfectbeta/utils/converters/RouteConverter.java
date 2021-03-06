package pl.ftims.ias.perfectbeta.utils.converters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.PhotoDTO;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.RatingDTO;
import pl.ftims.ias.perfectbeta.dto.routes_dtos.RouteDTO;
import pl.ftims.ias.perfectbeta.entities.PhotoEntity;
import pl.ftims.ias.perfectbeta.entities.RatingEntity;
import pl.ftims.ias.perfectbeta.entities.RouteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RouteConverter {

    public static RouteDTO climbingWallEntityToDTO(RouteEntity entity) {
        return new RouteDTO(entity.getId(), entity.getVersion(), entity.getRouteName(), entity.getDifficulty(), entity.getDescription(), entity.getHoldsDetails(), entity.getAvgRating(),
                entity.getClimbingGym().getId(), photoListDTOFromEntity(entity.getPhotos()));
    }


    public static PhotoDTO photoEntityToDTO(PhotoEntity entity) {
        return new PhotoDTO(entity.getId(), entity.getVersion(), entity.getPhotoUrl(), entity.getRoute().getId());
    }

    public static RatingDTO ratingEntityToDTO(RatingEntity entity) {
        return new RatingDTO(entity.getId(), entity.getVersion(), entity.getComment(),entity.getRate(),  entity.getRoute().getId(),entity.getUser().getId(),entity.getUser().getLogin());
    }

    public static List<PhotoDTO> photoListDTOFromEntity(List<PhotoEntity> photoEntities) {

        return null == photoEntities ? null : photoEntities.stream()
                .filter(Objects::nonNull)
                .map(RouteConverter::photoEntityToDTO)
                .collect(Collectors.toList());


    }
 public static List<RatingDTO> ratingListDTOFromEntity(List<RatingEntity> ratingEntities) {

        return null == ratingEntities ? null : ratingEntities.stream()
                .filter(Objects::nonNull)
                .map(RouteConverter::ratingEntityToDTO)
                .collect(Collectors.toList());
    }


    public static Page<RouteDTO> climbingWallEntityPageToDTOPage(Page<RouteEntity> entity) {

        List<RouteEntity> entities = entity.getContent();

        List<RouteDTO> dtos = new ArrayList<>();
        for (RouteEntity e : entities) {
            dtos.add(climbingWallEntityToDTO(e));
        }
        Page<RouteDTO> page = new PageImpl<RouteDTO>(dtos, entity.getPageable(), dtos.size());
        return page;
    }
}
