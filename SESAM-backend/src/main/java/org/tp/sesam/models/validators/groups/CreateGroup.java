package org.tp.sesam.models.validators.groups;

/**
 * Groupe de validation activé lors d'une CRÉATION (POST).
 * <p>
 * Permet de différencier les contraintes de validation selon l'opération :
 *
 * @NotBlank(groups = CreateGroup.class)  → obligatoire seulement à la création
 * @NotNull → obligatoire toujours (Default group)
 * <p>
 * AbstractService.create() active les groupes {Default, CreateGroup}.
 */
public interface CreateGroup {
}
