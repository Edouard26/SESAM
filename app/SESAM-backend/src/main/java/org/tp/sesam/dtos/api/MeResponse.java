package org.tp.sesam.dtos.api;
import org.tp.sesam.models.enums.Role;
/** Jamais de mot de passe, hash, token de renouvellement ou entité User dans la réponse. */
public record MeResponse(Long id, String nom, String prenom, String email, Role role, boolean active) { }
