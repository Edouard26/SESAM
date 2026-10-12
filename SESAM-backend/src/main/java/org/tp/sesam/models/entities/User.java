package org.tp.sesam.models.entities;

import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;
import org.tp.sesam.models.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Compte générique sans relation métier. Comme dans le ZIP : connexion par username.
 *
 *
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk__users__email", columnNames = "email"))
@Getter
@Setter
public class User extends AbstractPersistableWithIdSetter<Long> implements UserDetails {
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nom;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String prenom;

    @NotNull
    @Past
    @Column(nullable = false)
    private LocalDate dateNaissance;


    /**
     * Hash BCrypt uniquement. Ne pas exposer cette entité directement dans une réponse REST.
     */
    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Email
    @Size(max = 254)
    @Column(length = 254)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean actif = true;

    @Override
    public String getUsername() {
        // Spring Security utilise cette valeur comme identifiant de connexion.
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
