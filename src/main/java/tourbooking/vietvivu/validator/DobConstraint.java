package tourbooking.vietvivu.validator;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Target;

import jakarta.validation.Payload;

@Target({FIELD})
public @interface DobConstraint {
    String message() default "Invalid date of birth";

    int min();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
