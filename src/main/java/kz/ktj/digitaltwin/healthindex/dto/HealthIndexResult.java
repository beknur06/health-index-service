package kz.ktj.digitaltwin.healthindex.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Результат расчёта индекса здоровья локомотива.
 *
 * score:    0–100 (100 = идеально)
 * category: NORMAL / ATTENTION / CRITICAL
 * factors:  top-5 параметров, влияющих на снижение оценки
 * trend:    STABLE / DEGRADING / IMPROVING
 */
@Data
@Builder
public class HealthIndexResult {

    private String locomotiveId;
    private String locomotiveType;
    private Instant calculatedAt;

    /** Итоговый скор 0..100 */
    private double score;

    /** Категория по скору */
    private HealthCategory category;

    /** Тренд (сравнение с предыдущими N значениями) */
    private HealthTrend trend;

    /** Top-5 факторов, снижающих индекс (отсортированы по impact) */
    private List<FactorContribution> topFactors;

    /** Суммарный штраф от активных DTC-кодов */
    private double dtcPenalty;

    /** Количество активных алертов */
    private int activeAlerts;

    public enum HealthCategory {
        NORMAL,     // 75–100
        ATTENTION,  // 50–74
        CRITICAL    // 0–49
    }

    public enum HealthTrend {
        IMPROVING,
        STABLE,
        DEGRADING
    }

    /**
     * Вклад одного параметра в снижение индекса.
     * Объяснимость: машинист видит «coolant_temp: -12.4 балла».
     */
    @Data
    @Builder
    public static class FactorContribution {
        private String paramName;
        private String displayName;
        private double rawValue;
        private double normalizedDeviation; // 0..1
        private double weight;
        private double impact;             // отрицательный вклад в скор
        private String severity;           // NORMAL / WARNING / CRITICAL
    }
}
