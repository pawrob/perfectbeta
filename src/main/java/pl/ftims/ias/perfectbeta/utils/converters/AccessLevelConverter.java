package pl.ftims.ias.perfectbeta.utils.converters;

import lombok.NoArgsConstructor;
import pl.ftims.ias.perfectbeta.dto.AccessLevelDTO;
import pl.ftims.ias.perfectbeta.entities.AccessLevelEntity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor
public class AccessLevelConverter {

    public static AccessLevelDTO AccessLevelDTOFromEntity(AccessLevelEntity accessLevelEntity) {
        return new AccessLevelDTO(accessLevelEntity.getId(), accessLevelEntity.getVersion(),
                accessLevelEntity.getAccessLevel(), accessLevelEntity.getActive());
    }

    public static List<AccessLevelDTO> AccessLevelDTOListFromEntities(Collection<AccessLevelEntity> accessLevelEntities) {
        return null == accessLevelEntities ? null : accessLevelEntities.stream()
                .filter(Objects::nonNull)
                .map(AccessLevelConverter::AccessLevelDTOFromEntity)
                .collect(Collectors.toList());
    }
}
