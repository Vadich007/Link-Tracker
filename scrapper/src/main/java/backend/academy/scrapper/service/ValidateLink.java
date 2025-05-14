package backend.academy.scrapper.service;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LinkValidator.class)
public @interface ValidateLink {
    String message() default "Поддерживается только GitHub и Stackoverflow";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
