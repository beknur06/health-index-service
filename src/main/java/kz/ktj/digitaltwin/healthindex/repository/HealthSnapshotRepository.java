package kz.ktj.digitaltwin.healthindex.repository;

import kz.ktj.digitaltwin.healthindex.entity.HealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface HealthSnapshotRepository extends JpaRepository<HealthSnapshot, UUID> {

    List<HealthSnapshot> findTop30ByLocomotiveIdOrderByCalculatedAtDesc(String locomotiveId);

    List<HealthSnapshot> findByLocomotiveIdAndCalculatedAtBetweenOrderByCalculatedAtAsc(
        String locomotiveId, Instant from, Instant to);
}
