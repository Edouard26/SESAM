package org.tp.sesam.services.commons;
/** Le contrat HTTP existe mais son traitement métier reste à écrire. */
public class FonctionnaliteNonImplementeeException extends RuntimeException {
    public FonctionnaliteNonImplementeeException(String operation) {
        super("Traitement métier à implémenter : " + operation);
    }
}
