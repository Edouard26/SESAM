package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import net.ent.etnc.backend.models.enums.Grade;

import java.time.LocalDate;

@Getter
@Setter
public class StagiaireRequest {
    private String nom;
    private String prenom;
    private String nid;
    private String email;
    private LocalDate dateNaissance;
    private Grade grade;
}
