package org.tp.sesam.commons;

import org.tp.sesam.services.commons.ServiceException;
import tools.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.Set;

/**
 * Applique un JSON Patch (RFC 6902) sur un DTO, en contrôlant les champs autorisés (whitelist).
 * <p>
 * Le patch cible les attributs du DTO (Payload). La correspondance avec les attributs de l'entité
 * (qui peuvent différer, ex. {@code trainId} ↔ {@code train}) est ensuite assurée par l'assembler
 * ({@code toEntity}). Cette version utilise Jackson 3 (Spring Boot 4). Le patch est traité via
 * Jakarta JSON-P, l'ObjectMapper ne sert qu'à convertir DTO ↔ JSON.
 */
@Component
public class JsonPatchUtils {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonPatchUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @param patchJson       le corps de la requête (tableau d'opérations RFC 6902)
     * @param currentDto      l'état courant de la ressource, en représentation DTO
     * @param dtoType         le type du DTO à reconstruire
     * @param patchableFields les champs (1er segment du {@code path}) autorisés à être modifiés
     */
    public <P> P apply(String patchJson, P currentDto, Class<P> dtoType, Set<String> patchableFields) {
        JsonArray operations = parseOperations(patchJson);
        enforceWhitelist(operations, patchableFields);
        return applyPatch(operations, currentDto, dtoType);
    }

    private JsonArray parseOperations(String patchJson) {
        try (var reader = Json.createReader(new StringReader(patchJson))) {
            return reader.readArray();
        } catch (RuntimeException e) {
            throw new ServiceException("Patch invalide : corps JSON Patch illisible.", e);
        }
    }

    private void enforceWhitelist(JsonArray operations, Set<String> patchableFields) {
        for (JsonValue value : operations) {
            if (value.getValueType() != JsonValue.ValueType.OBJECT) {
                throw new ServiceException("Patch invalide : chaque opération doit être un objet.");
            }
            JsonObject op = value.asJsonObject();
            // Restriction choisie par cette API : uniquement replace sur un champ simple du DTO.
            String operation = op.getString("op", null);
            if (!"replace".equals(operation)) {
                throw new ServiceException("Opération non supportée : " + operation
                        + " (seul 'replace' est autorisé).");
            }
            if (!op.containsKey("value")) throw new ServiceException("Valeur du patch obligatoire");
            checkField(op.getString("path", null), patchableFields);
        }
    }

    private void checkField(String pointer, Set<String> patchableFields) {
        // Un seul segment : pas de modification imbriquée ni d'échappement ambigu.
        if (pointer == null || !pointer.startsWith("/") || pointer.length() < 2
                || pointer.indexOf('/', 1) >= 0 || pointer.contains("~")) {
            throw new ServiceException("Chemin JSON Patch invalide : champ simple attendu");
        }
        String field = pointer.substring(1);
        if (!patchableFields.contains(field)) {
            throw new ServiceException("Champ non patchable : " + field);
        }
    }

    private <P> P applyPatch(JsonArray operations, P currentDto, Class<P> dtoType) {
        try {
            JsonObject target;
            try (var reader = Json.createReader(new StringReader(objectMapper.writeValueAsString(currentDto)))) {
                target = reader.readObject();
            }
            JsonObject patched = Json.createPatch(operations).apply(target);
            return objectMapper.readValue(patched.toString(), dtoType);
        } catch (JsonException e) {
            throw new ServiceException("Patch invalide : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceException("Patch invalide : impossible d'appliquer le patch.", e);
        }
    }
}
