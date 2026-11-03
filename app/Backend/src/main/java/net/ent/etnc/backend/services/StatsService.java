package net.ent.etnc.backend.services;

import net.ent.etnc.backend.dtos.responses.StatsResponse;
import org.springframework.transaction.annotation.Transactional;

public interface StatsService {
    @Transactional(readOnly = true)
    StatsResponse getStats();
}
