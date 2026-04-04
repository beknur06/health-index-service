package kz.ktj.digitaltwin.healthindex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryEnvelope {
    private String messageId;
    private Instant receivedAt;
    private String locomotiveId;
    private String locomotiveType;
    private Instant timestamp;
    private String phase;
    private Map<String, Double> parameters;
    private List<String> activeDtcCodes;
    private Double gpsLat;
    private Double gpsLon;
    private String routeId;
    private Double odometer;
}
