package org.tp.sesam.services.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Erreur métier. Le handler HTTP ne publie jamais la chaîne technique des causes.
 */
public class ServiceException extends RuntimeException {
    private final String code;

    public ServiceException(String message) {
        this(null, message, null);
    }

    public ServiceException(String code, String message) {
        this(code, message, null);
    }

    public ServiceException(String message, Throwable cause) {
        this(null, message, cause);
    }

    private ServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public List<String> getCausesMessage() {
        List<String> messages = new ArrayList<>();
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = this; current != null && seen.add(current); current = current.getCause()) {
            if (current.getMessage() != null) messages.add(current.getMessage());
        }
        return List.copyOf(messages);
    }
}
