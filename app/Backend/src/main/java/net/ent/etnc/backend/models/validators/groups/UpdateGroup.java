package net.ent.etnc.backend.models.validators.groups;

/**
 * Groupe de validation Bean Validation activé lors de la mise à jour d'une entité.
 *
 * <p>Utilisé avec {@code @Validated(UpdateGroup.class)} dans les controllers
 * et avec {@code groups = {UpdateGroup.class}} sur les contraintes qui ne s'appliquent
 * qu'à la mise à jour (ex: {@code id} et {@code version} obligatoires).
 */
public interface UpdateGroup {
}
