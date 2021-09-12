package pl.ftims.ias.your_climbing_gym.mok.endpoints;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ftims.ias.your_climbing_gym.dto.user_dtos.RegistrationDTO;
import pl.ftims.ias.your_climbing_gym.dto.user_dtos.UserWithPersonalDataAccessLevelDTO;
import pl.ftims.ias.your_climbing_gym.mok.services.ManagerService;
import pl.ftims.ias.your_climbing_gym.utils.converters.UserConverter;

import javax.validation.Valid;

@RestController
@RequestMapping("managers")
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.NEVER)
public class ManagerEndpoint {

    ManagerService managerService;
    RetryTemplate retry;

    @Autowired
    public ManagerEndpoint(ManagerService managerService, RetryTemplate retry) {
        this.managerService = managerService;
        this.retry = retry;
    }

    @Secured("ROLE_ADMINISTRATOR")
    @PostMapping("register")
    public UserWithPersonalDataAccessLevelDTO registerManager(@RequestBody @Valid RegistrationDTO user){
        return retry.execute(arg0 -> UserConverter.userWithPersonalDataAccessLevelDTOFromEntity(
                managerService.createManagerAccountWithAccessLevel(UserConverter.createNewUserEntityFromDTO(user))));

    }
}
