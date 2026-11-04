package net.ent.etnc.backend.models.validators.dateRange;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contrainte de validation vérifiant que la date de début est antérieure à la date de fin.
 *
 * <p>Applicable au niveau classe (type-level). Les noms des champs à comparer
 * sont fournis via les attributs {@link #start()} et {@link #end()}.</p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {

    /** Message d'erreur retourné si la contrainte est violée. */
    String message() default "La date de début doit être antérieure à la date de fin";

    /** Groupes de validation auxquels cette contrainte appartient. */
    Class<?>[] groups() default {};

    /** Payload de la contrainte (métadonnées optionnelles pour les clients). */
    Class<? extends Payload>[] payload() default {};

    /** Nom du champ représentant la date de début dans la classe annotée. */
    String start() default "";

    /** Nom du champ représentant la date de fin dans la classe annotée. */
    String end() default "";
}
