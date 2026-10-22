package net.ent.etnc.backend.dtos.responses;

import lombok.*;
import net.ent.etnc.backend.models.enums.Grade;
import net.ent.etnc.backend.models.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagiaireResponse {
    private Long id;

    private String nid;
    private String email;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Grade grade;
    private Role role;
    private Boolean active;

    private List<Long> materiels;
}
