package kz.ktj.digitaltwin.healthindex.controller;

import kz.ktj.digitaltwin.healthindex.entity.HealthParamWeight;
import kz.ktj.digitaltwin.healthindex.entity.HealthSnapshot;
import kz.ktj.digitaltwin.healthindex.repository.HealthParamWeightRepository;
import kz.ktj.digitaltwin.healthindex.repository.HealthSnapshotRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST API для индекса здоровья.
 *
 *   GET  /api/v1/health/config          — текущие веса
 *   PUT  /api/v1/health/config/{id}     — обновить вес (без перекомпиляции!)
 *   GET  /api/v1/health/{locoId}/history — история снимков
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthIndexController {

    private final HealthParamWeightRepository weightRepository;
    private final HealthSnapshotRepository snapshotRepository;

    public HealthIndexController(HealthParamWeightRepository weightRepository,
                                  HealthSnapshotRepository snapshotRepository) {
        this.weightRepository = weightRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * GET /api/v1/health/config
     * Получить все текущие веса и пороги.
     */
    @GetMapping("/config")
    public ResponseEntity<List<HealthParamWeight>> getConfig() {
        return ResponseEntity.ok(weightRepository.findAll());
    }

    /**
     * PUT /api/v1/health/config/{id}
     * Обновить вес/порог конкретного параметра.
     * Изменения подхватываются калькулятором через кеш (TTL 1 мин).
     */
    @PutMapping("/config/{id}")
    public ResponseEntity<HealthParamWeight> updateWeight(
            @PathVariable java.util.UUID id,
            @RequestBody HealthParamWeight update) {
        return weightRepository.findById(id)
            .map(existing -> {
                existing.setWeight(update.getWeight());
                existing.setPenaltyMultiplier(update.getPenaltyMultiplier());
                existing.setWarningThreshold(update.getWarningThreshold());
                existing.setCriticalThreshold(update.getCriticalThreshold());
                existing.setDisplayName(update.getDisplayName());
                return ResponseEntity.ok(weightRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/health/{locomotiveId}/history?from=...&to=...
     * История снимков индекса здоровья.
     */
    @GetMapping("/{locomotiveId}/history")
    public ResponseEntity<List<HealthSnapshot>> getHistory(
            @PathVariable String locomotiveId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<HealthSnapshot> snapshots = snapshotRepository
            .findByLocomotiveIdAndCalculatedAtBetweenOrderByCalculatedAtAsc(locomotiveId, from, to);
        return ResponseEntity.ok(snapshots);
    }

    /**
     * GET /api/v1/health/{locomotiveId}/latest
     * Последние 30 снимков (для быстрого тренда).
     */
    @GetMapping("/{locomotiveId}/latest")
    public ResponseEntity<List<HealthSnapshot>> getLatest(@PathVariable String locomotiveId) {
        return ResponseEntity.ok(
            snapshotRepository.findTop30ByLocomotiveIdOrderByCalculatedAtDesc(locomotiveId));
    }
}
