package pl.ftims.ias.your_climbing_gym.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidation.class)
public @interface Email {
    String message() default "Email is not valid";
    Class<?>[] groups() default {};
    public abstract Class<? extends Payload>[] payload() default {};
}