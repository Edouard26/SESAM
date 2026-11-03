package net.ent.etnc.backend.models.validators.dateRange;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implémentation de la contrainte {@link ValidDateRange}.
 *
 * <p>Accède aux champs nommés dans l'annotation par réflexion et vérifie
 * que la date de début est strictement antérieure ou égale à la date de fin.
 * La contrainte est satisfaite si l'un des deux champs est {@code null}
 * (délégation aux contraintes {@code @NotNull} individuelles).
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startFieldName;
    private String endFieldName;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startFieldName = constraintAnnotation.start();
        this.endFieldName = constraintAnnotation.end();
    }

    /**
     * Vérifie que la valeur du champ {@code start} est antérieure ou égale à celle du champ {@code end}.
     *
     * @param value   l'objet sur lequel est posée l'annotation
     * @param context contexte de validation (non utilisé ici)
     * @return {@code true} si la plage de dates est valide ou si un champ est {@code null}
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Object startValue = getFieldValue(value, startFieldName);
            Object endValue = getFieldValue(value, endFieldName);

            if (startValue == null || endValue == null) return true;

            LocalDateTime start = toLocalDateTime(startValue);
            LocalDateTime end = toLocalDateTime(endValue);

            if (start == null || end == null) return true;

            return start.isBefore(end) || start.isEqual(end);
        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof LocalDate ld) return ld.atStartOfDay();
        return null;
    }
}
