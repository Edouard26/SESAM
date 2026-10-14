package net.ent.etnc.backend.services.commons;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception métier lancée par les services lorsqu'une règle de gestion est violée.
 *
 * RÈGLE : Toujours utiliser ServiceException plutôt qu'une exception brute.
 * Les messages de ServiceException sont retournés directement au client HTTP
 * via le GlobalExceptionHandler — ils doivent donc être lisibles par l'utilisateur.
 *
 * Exemple : throw new ServiceException("RG-03 : Niveau de sécurité insuffisant pour cette espèce.");
 *
 * getCausesMessage() remonte récursivement tous les messages de la chaîne de cause,
 * ce qui est utile pour déboguer les erreurs Hibernate enchaînées.
 */
public class ServiceException extends RuntimeException {

    @Getter
    private List<String> causesMessage = new ArrayList<>();

    /** Code identifiant la règle métier violée (ex: {@code "RG-U01"}). Peut être {@code null}. */
    @Getter
    private String code;

    public ServiceException(String message) {
        super(message);
        this.causesMessage = List.of(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.causesMessage = extractCausesMessages(cause);
    }

    /**
     * Crée une exception avec un code de règle métier et un message.
     *
     * @param code    l'identifiant de la règle violée (ex: {@code "RG-U01"})
     * @param message le message décrivant l'erreur
     */
    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
        this.causesMessage = List.of(message);
    }

    // Remonte récursivement les messages de toutes les causes enchaînées
    private List<String> extractCausesMessages(Throwable cause) {
        if (cause == null) {
            return new ArrayList<>();
        }
        List<String> messages = extractCausesMessages(cause.getCause());
        messages.add(cause.getMessage());
        return messages;
    }
}
