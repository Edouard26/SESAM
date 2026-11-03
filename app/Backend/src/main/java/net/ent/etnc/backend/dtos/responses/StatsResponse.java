package net.ent.etnc.backend.dtos.responses;

import java.util.Map;

public record StatsResponse(
        Materiels materiels,
        Salles salles,
        Alertes alertes,
        Stages stages
) {
    public record Materiels(
            long total,
            Map<String, Long> parType,
            Map<String, Long> parEtat,
            long garantieExpiree,
            long garantieExpirantSous30Jours
    ) {}

    public record Salles(
            long total,
            long salleCours,
            long salleStockage,
            Map<String, Long> parEtat
    ) {}

    public record Alertes(
            long total,
            Map<String, Long> parStatut
    ) {}

    public record Stages(
            long total,
            Map<String, Long> parStatut
    ) {}
}
