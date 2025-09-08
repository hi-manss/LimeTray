package com.api.order.mq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {

    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void sendOrder(Long orderId) {
        log.info("Sending order event for orderId: {}", orderId);
        kafkaTemplate.send(TOPIC, orderId);
    }
}
