package net.ent.etnc.backend.dtos;

import lombok.*;
import net.ent.etnc.backend.models.enums.Grade;
import net.ent.etnc.backend.models.enums.Role;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    private String nid;
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Grade grade;
    private Role role;
    private Boolean active;
}
