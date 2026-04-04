package kz.ktj.digitaltwin.healthindex.listener;

import kz.ktj.digitaltwin.healthindex.dto.HealthIndexResult;
import kz.ktj.digitaltwin.healthindex.dto.TelemetryEnvelope;
import kz.ktj.digitaltwin.healthindex.service.HealthIndexCalculator;
import kz.ktj.digitaltwin.healthindex.service.HealthIndexPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryListener {

    private static final Logger log = LoggerFactory.getLogger(TelemetryListener.class);

    private final HealthIndexCalculator calculator;
    private final HealthIndexPublisher publisher;

    public TelemetryListener(HealthIndexCalculator calculator, HealthIndexPublisher publisher) {
        this.calculator = calculator;
        this.publisher = publisher;
    }

    @RabbitListener(queues = "${rabbitmq.queue.health-index}")
    public void onMessage(TelemetryEnvelope envelope) {
        if (envelope == null || envelope.getLocomotiveId() == null) {
            log.warn("Received null envelope, skipping");
            return;
        }

        try {
            HealthIndexResult result = calculator.calculate(envelope);
            publisher.publish(result);
        } catch (Exception e) {
            log.error("Health index calculation failed for [{}]: {}",
                envelope.getMessageId(), e.getMessage(), e);
        }
    }
}
