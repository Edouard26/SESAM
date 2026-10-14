package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;
import net.ent.etnc.backend.models.enums.Grade;
import net.ent.etnc.backend.models.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Entité représentant un utilisateur de la plateforme.
 *
 * <p>Implémente {@link UserDetails} pour l'intégration avec Spring Security.
 * L'email est utilisé comme identifiant de connexion (username).
 * Les autorités sont calculées transitivement depuis le {@link Role} de l'utilisateur,
 * ce qui permet à un ADMIN d'être reconnu comme RESPONSABLE, FORMATEUR, etc.
 *
 * <p>Règles métier clés :
 * <ul>
 *   <li>RG-U01 : unicité de l'email</li>
 *   <li>RG-U03 : le dernier administrateur actif ne peut pas être désactivé</li>
 * </ul>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk__users__NID", columnNames = "NID")
)
@EqualsAndHashCode(callSuper = false, of = {"nid"})
@ToString(callSuper = true, of = {"nid", "email","nom", "prenom", "dateNaissance", "grade", "role", "active"})
public class User extends AbstractPersistableWithIdSetter<Long> implements UserDetails {

    //LBK
    @Getter
    @Setter
    //BV
    @NotBlank(message = "NID ne peut pas être vide")
    @Pattern(regexp = "^[0-9]{10}$", message = "Le NID doit être composer uniquement de 10 chiffres")
    //JPA
    @Column(name = "NID", nullable = false, length = 10)
    private String nid;

    @Getter
    @Setter
    @NotBlank(message = "L'email est obligatoire")
    @Email
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /** Mot de passe stocké sous forme de hash BCrypt. */
    @Getter
    @Setter
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(name = "password", nullable = false)
    private String password;

    //LBK
    @Getter
    @Setter
    //BV
    @NotBlank(message = "nom ne peut pas être vide")
    @Size(min = 2, max = 20, message = "nom doit faire 2 caractères minimum et 30 maximum")
    //JPA
    @Column(name = "nom", nullable = false, length = 30)
    private String nom;

    //LBK
    @Getter
    @Setter
    //BV
    @NotBlank(message = "prénom ne peut pas être vide")
    @Size(min = 2, max = 20, message = "prénom doit faire 2 caractères minimum et 30 maximum")
    //JPA
    @Column(name = "prenom", nullable = false, length = 30)
    private String prenom;

    //LBK
    @Getter
    @Setter
    //BV
    @NotNull(message = "date de naissance ne peut pas être null")
    //JPA
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    //LBK
    @Getter
    @Setter
    //BV
    @NotNull(message = "grade ne peut pas être null")
    //JPA
    @Enumerated(value = EnumType.STRING)
    @Column(name = "grade", nullable = false, length = 30)
    private Grade grade;

    @Getter
    @Setter
    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /** Indique si le compte est actif. Un compte inactif ne peut pas se connecter. */
    @Getter
    @Setter
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * Retourne les autorités Spring Security de cet utilisateur.
     * Inclut le rôle propre et tous les rôles hérités (hiérarchie cumulative).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAllRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    /** Un compte inactif est considéré comme désactivé par Spring Security. */
    @Override
    public boolean isEnabled() {
        return active;
    }
}
