package kz.ktj.digitaltwin.healthindex.repository;

import kz.ktj.digitaltwin.healthindex.entity.HealthParamWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HealthParamWeightRepository extends JpaRepository<HealthParamWeight, UUID> {

    List<HealthParamWeight> findByApplicableToInOrderByWeightDesc(List<String> types);
}
