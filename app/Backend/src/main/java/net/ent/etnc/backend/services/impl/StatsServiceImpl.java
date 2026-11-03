package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.dtos.responses.StatsResponse;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.models.enums.TypeMateriel;
import net.ent.etnc.backend.models.enums.TypeSalle;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.repositories.EcranRepository;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.repositories.VideoProjecteurRepository;
import net.ent.etnc.backend.services.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToLongFunction;

@Service
public class StatsServiceImpl implements StatsService {
    private final MaterielRepository materielRepository;
    private final EcranRepository ecranRepository;
    private final UniteCentraleRepository uniteCentraleRepository;
    private final VideoProjecteurRepository videoProjecteurRepository;
    private final SalleRepository salleRepository;
    private final AlerteRepository alerteRepository;
    private final StageRepository stageRepository;

    public StatsServiceImpl(MaterielRepository materielRepository,
                            EcranRepository ecranRepository,
                            UniteCentraleRepository uniteCentraleRepository,
                            VideoProjecteurRepository videoProjecteurRepository,
                            SalleRepository salleRepository,
                            AlerteRepository alerteRepository,
                            StageRepository stageRepository) {
        this.materielRepository = materielRepository;
        this.ecranRepository = ecranRepository;
        this.uniteCentraleRepository = uniteCentraleRepository;
        this.videoProjecteurRepository = videoProjecteurRepository;
        this.salleRepository = salleRepository;
        this.alerteRepository = alerteRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public StatsResponse getStats() {
        LocalDate today = LocalDate.now();

        Map<String, Long> materielsParType = new LinkedHashMap<>();
        materielsParType.put(TypeMateriel.ECRAN.name(), ecranRepository.count());
        materielsParType.put(TypeMateriel.UC.name(), uniteCentraleRepository.count());
        materielsParType.put(TypeMateriel.VIDEO_PROJECTEUR.name(), videoProjecteurRepository.count());

        return new StatsResponse(
                new StatsResponse.Materiels(
                        materielRepository.count(),
                        materielsParType,
                        countByEnum(EtatMateriel.values(), materielRepository::countByEtat),
                        materielRepository.countByDateFinGarantieBefore(today),
                        materielRepository.countByDateFinGarantieBetween(today, today.plusDays(30))
                ),
                new StatsResponse.Salles(
                        salleRepository.count(),
                        salleRepository.countByTypeSalle(TypeSalle.COURS),
                        salleRepository.countByTypeSalle(TypeSalle.STOCKAGE),
                        countByEnum(EtatSalle.values(), salleRepository::countByEtat)
                ),
                new StatsResponse.Alertes(
                        alerteRepository.count(),
                        countByEnum(StatutAlerte.values(), alerteRepository::countByStatut)
                ),
                new StatsResponse.Stages(
                        stageRepository.count(),
                        countByEnum(StatutStage.values(), stageRepository::countByStatut)
                )
        );
    }

    private static <E extends Enum<E>> Map<String, Long> countByEnum(E[] values, ToLongFunction<E> counter) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (E value : values) {
            result.put(value.name(), counter.applyAsLong(value));
        }
        return result;
    }
}
