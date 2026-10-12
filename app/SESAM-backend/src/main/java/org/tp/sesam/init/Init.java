package org.tp.sesam.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Point d'entrée des données de test : aucun compte/mot de passe n'est créé automatiquement.
 */
@Component
public class Init implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // TODO : injecter les services nécessaires et créer les données du TP.
        // Pour User : passwordEncoder.encode(motDePasse) avant persistance.
    }
}
