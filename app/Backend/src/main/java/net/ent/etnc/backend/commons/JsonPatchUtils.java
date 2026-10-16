package net.ent.etnc.backend.commons;

import jakarta.json.*;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;

/**
 * Applique un JSON Patch (RFC 6902) sur un DTO, en contrôlant les champs autorisés (whitelist).
 *
 * Le patch cible les attributs du DTO (Payload). La correspondance avec les attributs de l'entité
 * (qui peuvent différer, ex. {@code trainId} ↔ {@code train}) est ensuite assurée par l'assembler
 * ({@code toEntity}). On reste indépendant de la version de Jackson : le patch est traité via
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
        JsonObject operations = parseOperations(patchJson);
        enforceWhitelist(operations, patchableFields);
        return applyPatch(operations, currentDto, dtoType);
    }

    private JsonObject parseOperations(String patchJson) {
        try (var reader = Json.createReader(new StringReader(patchJson))) {
            return reader.readObject();
        } catch (RuntimeException e) {
            throw new ServiceException("Patch invalide : corps JSON Patch illisible." + e.getMessage(), e);
        }
    }

    private void enforceWhitelist(JsonObject operations, Set<String> patchableFields) {

        for (Map.Entry<String, JsonValue> value : operations.entrySet()) {
            if (value.getValue().getValueType() == JsonValue.ValueType.OBJECT) {
                throw new ServiceException("Patch invalide : une opération doit pas être un objet.");
            }
            String key = value.getKey();

            checkField(key, patchableFields);
        }
    }

    private void checkField(String pointer, Set<String> patchableFields) {
        if (pointer == null) {
            return;
        }
        if (!patchableFields.contains(pointer)) {
            throw new ServiceException("Champ non patchable : " + pointer);
        }
    }


    private <P> P applyPatch(JsonObject operations, P currentDto, Class<P> dtoType) {
        try {
            JsonObject target;
            try (var reader = Json.createReader(new StringReader(objectMapper.writeValueAsString(currentDto)))) {
                target = reader.readObject();
            }
            JsonObject patched = (JsonObject) Json.createMergePatch(operations).apply(target);
            return objectMapper.readValue(patched.toString(), dtoType);
        } catch (JsonException e) {
            throw new ServiceException("Patch invalide : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceException("Patch invalide : impossible d'appliquer le patch.", e);
        }
    }
}
