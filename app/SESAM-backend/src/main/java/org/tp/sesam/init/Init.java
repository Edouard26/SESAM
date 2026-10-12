package org.tp.sesam.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.models.enums.Role;
import org.tp.sesam.repositories.UserRepository;

import java.time.LocalDate;

@Component
@Profile("dev") // Initialisation réservée au développement.
public class Init implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public Init(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_PASSWORD}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String email = "admin@sesam.fr";

        // Ne recrée pas le compte et ne modifie pas son mot de passe.
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setNom("Admin");
        admin.setPrenom("SESAM");
        admin.setDateNaissance(LocalDate.of(1990, 1, 1));
        admin.setEmail(email);
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setActif(true);

        // Enregistre le hash BCrypt, jamais le mot de passe en clair.
        admin.setPassword(passwordEncoder.encode(adminPassword));

        userRepository.save(admin);
    }
}