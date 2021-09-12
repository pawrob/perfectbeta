package pl.ftims.ias.your_climbing_gym.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.ftims.ias.your_climbing_gym.dto.validation.Password;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialsDTO implements Serializable {

    @NotNull
    @NotBlank
    private String username;
    @NotNull
    @NotBlank
    @Password
    private String password;

}
