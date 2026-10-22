package org.tp.sesam.models.validators.dateRange;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import org.springframework.beans.BeanWrapperImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Début <= fin. Les valeurs nulles sont contrôlées séparément par @NotNull.
 */
public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private String start;
    private String end;

    @Override
    public void initialize(ValidDateRange annotation) {
        start = annotation.start();
        end = annotation.end();
        if (start.isBlank() || end.isBlank()) throw new ValidationException("Champs start/end obligatoires");
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        var bean = new BeanWrapperImpl(value);
        Object a = bean.getPropertyValue(start), b = bean.getPropertyValue(end);
        if (a == null || b == null) return true;
        return !convert(a).isAfter(convert(b));
    }

    private LocalDateTime convert(Object value) {
        if (value instanceof LocalDateTime date) return date;
        if (value instanceof LocalDate date) return date.atStartOfDay();
        throw new ValidationException("ValidDateRange attend LocalDate ou LocalDateTime");
    }
}
