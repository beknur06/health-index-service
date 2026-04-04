package kz.ktj.digitaltwin.healthindex.service;

import kz.ktj.digitaltwin.healthindex.dto.HealthIndexResult.HealthTrend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Отслеживает тренд индекса здоровья за последние N значений.
 *
 * Простая линейная регрессия по скользящему окну:
 *   - Если slope > +0.5 за 30 тиков → IMPROVING
 *   - Если slope < -0.5 → DEGRADING
 *   - Иначе → STABLE
 */
@Service
public class TrendTracker {

    private final int windowSize;
    private final ConcurrentHashMap<String, Deque<Double>> windows = new ConcurrentHashMap<>();

    public TrendTracker(@Value("${health-index.trend-window-size:30}") int windowSize) {
        this.windowSize = windowSize;
    }

    /**
     * Добавляет новый скор и возвращает текущий тренд.
     */
    public HealthTrend updateAndGetTrend(String locomotiveId, double score) {
        Deque<Double> window = windows.computeIfAbsent(locomotiveId, k -> new ArrayDeque<>());

        window.addLast(score);
        if (window.size() > windowSize) {
            window.removeFirst();
        }

        if (window.size() < 5) {
            return HealthTrend.STABLE; // недостаточно данных
        }

        // Линейная регрессия: y = a + b*x
        double slope = calculateSlope(window);

        if (slope > 0.5) return HealthTrend.IMPROVING;
        if (slope < -0.5) return HealthTrend.DEGRADING;
        return HealthTrend.STABLE;
    }

    /**
     * Наклон линейной регрессии (метод наименьших квадратов).
     * slope > 0 = скор растёт (улучшение)
     * slope < 0 = скор падает (деградация)
     */
    private double calculateSlope(Deque<Double> values) {
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        int i = 0;
        for (Double y : values) {
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumX2 += (double) i * i;
            i++;
        }

        double denom = n * sumX2 - sumX * sumX;
        if (Math.abs(denom) < 1e-10) return 0;

        return (n * sumXY - sumX * sumY) / denom;
    }
}
