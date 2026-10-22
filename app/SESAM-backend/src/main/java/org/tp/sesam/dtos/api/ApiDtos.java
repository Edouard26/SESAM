package org.tp.sesam.dtos.api;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.tp.sesam.models.enums.Role;
import org.tp.sesam.models.enums.TypeMateriel;
import org.tp.sesam.models.enums.TypeSalle;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contrats des nouvelles actions. Les DTO du CRUD existant restent dans dtos.
 * Les types String de statut sont à aligner sur les futurs enums métier.
 * PATCH applicatif : champ null ou absent = inchangé ; version obligatoire.
 * Ce contrat n'est pas JSON Merge Patch ni JSON Patch RFC 6902.
 */
public final class ApiDtos {
    private ApiDtos() { }

    public record SalleFilter(String search, TypeSalle type, String building, String state,
            Boolean available, @Positive Long stageId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // La disponibilité d'un stage doit être vérifiée sur toute sa période.
        @AssertTrue(message = "Fournir stageId OU startDate/endDate pour available=true")
        public boolean isPeriodeValide() {
            boolean dates = startDate != null || endDate != null;
            if (stageId != null && dates) return false;
            if (dates && (startDate == null || endDate == null || endDate.isBefore(startDate))) return false;
            return !Boolean.TRUE.equals(available) || stageId != null || dates;
        }
    }
    public record MaterielFilter(String search, TypeMateriel type, @Positive Long materialId,
            String state, String connection, @Positive Long roomId, String building) { }
    public record StageFilter(String search, String status, @Positive Long roomId) { }
    public record UserFilter(String search, Role role, Boolean active, @Positive Long stageId) { }
    public enum AlerteStatus { PROCESSED, CANCELLED }
    public record AlerteFilter(String status) { }

    public record MaterielCreation(@NotBlank @Size(max=100) String numeroSerie,
            @NotNull @PastOrPresent LocalDate dateAjout, @NotNull LocalDate dateFinGarantie,
            @NotBlank @Size(max=100) String marque, @NotNull TypeMateriel type,
            @NotNull @Positive Long roomId) { }
    public record MaterielPatch(@NotNull @PositiveOrZero Long version,
            @Size(min=1,max=100) String numeroSerie, @PastOrPresent LocalDate dateAjout,
            LocalDate dateFinGarantie, @Size(min=1,max=100) String marque,
            TypeMateriel type, @Size(min=1,max=40) String state) {
        @AssertTrue(message="Fournir au moins un champ à modifier")
        public boolean isModificationPresente() {
            return numeroSerie != null || dateAjout != null || dateFinGarantie != null
                    || marque != null || type != null || state != null;
        }
    }
    public record AffectationMateriels(@NotEmpty List<@NotNull @Positive Long> materialIds,
            @NotNull @Positive Long roomId) { }
    public record StagePatch(@NotNull @PositiveOrZero Long version,
            @Size(min=1,max=100) String nom, LocalDate dateDebut, LocalDate dateFin,
            @Positive Long roomId) {
        @AssertTrue(message="Fournir au moins un champ à modifier")
        public boolean isModificationPresente() {
            return nom != null || dateDebut != null || dateFin != null || roomId != null;
        }
    }
    public record AffectationUsers(@NotEmpty List<@NotNull @Positive Long> userIds) { }
    public record AlerteCreation(@NotBlank String type,
            @NotNull @Size(max=150) String description,
            @NotEmpty List<@NotNull @Positive Long> materialIds) { }
    public record AlertePatch(@NotNull @PositiveOrZero Long version,
            @NotNull AlerteStatus status) { }
    public record UserCreation(@NotBlank @Size(max=50) String nom,
            @NotBlank @Size(max=50) String prenom, @NotNull @Past LocalDate dateNaissance,
            @NotBlank @Email @Size(max=254) String email,
            @NotBlank @Size(min=12,max=72) String password, @NotNull Role role,
            @NotNull Boolean active, @Positive Long stageId) { }
    public record UserPatch(@NotNull @PositiveOrZero Long version,
            @Size(min=1,max=50) String nom, @Size(min=1,max=50) String prenom,
            @Past LocalDate dateNaissance, @Email @Size(min=1,max=254) String email,
            @Size(min=12,max=72) String password, Role role, Boolean active,
            @Positive Long stageId) {
        @AssertTrue(message="Fournir au moins un champ à modifier")
        public boolean isModificationPresente() {
            return nom != null || prenom != null || dateNaissance != null || email != null
                    || password != null || role != null || active != null || stageId != null;
        }
    }
    // Une autorisation de processus est un choix booléen proposé pour ce contrat.
    public record ReglePatch(@NotNull Boolean autorise) { }
    public record RegleResponse(Long processId, String nom, boolean autorise, String origine) { }
    public record ProcessusObserve(Long processId, String nom, Long materialId,
            Boolean autorise, String origine) { }
    public record UserResponse(Long id, Long version, String nom, String prenom,
            LocalDate dateNaissance, String email, Role role, boolean active, Long stageId) { }
    public record AlerteResponse(Long id, Long version, String type, String description,
            List<Long> materialIds, String status) { }
    public record ReinstallationResponse(Long id, String status, List<Long> materialIds) { }
    public record DashboardResponse(Role role, Map<String,Long> statistiques,
            List<Long> roomIds, List<Long> materialIds, Long stageId,
            List<AlerteResponse> signalementsRecents) { }
    // Import JSON : références sélectionnées dans le système externe, pas un fichier envoyé.
    public record ImportRequest(@NotEmpty List<@NotBlank String> externalIds) { }
    public record ImportResponse(int imported, List<String> externalIds) { }
    public record ExportFile(String filename, String contentType, byte[] content) { }
    public record NotificationResponse(Long stageId, String status, Instant requestedAt) { }
}
