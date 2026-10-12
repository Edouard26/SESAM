package org.tp.sesam.models.validators.dateRange;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "La date de début doit être antérieur à la date de fin";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String start() default "";

    String end() default "";
}
