package com.api.order.mq;
import com.api.order.entity.Orders;
import com.api.order.enums.OrderStatus;
import com.api.order.repository.OrderRepository;
import com.api.order.service.OrderServiceDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderRepository ordersRepository;
    private final OrderServiceDetailService orderServiceDetailService;

    @KafkaListener(topics = "order-events", groupId = "order-service-group")
    public void consumeOrder(Long orderId) {
        log.info("Received order event for orderId: {}", orderId);

        ordersRepository.findById(orderId).ifPresent(order -> {
            try {
                Thread.sleep(2000);
                order.setStatus(OrderStatus.PROCESSING);
                order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                order.setNotes("Order is being processed");
                Orders fromDB = ordersRepository.save(order);

                orderServiceDetailService.saveOrderStatusLogs(fromDB, OrderStatus.PROCESSING, "System", "Order is being processed");

                log.info("Order {} status updated to PROCESSING", orderId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Order processing interrupted for order {}", orderId, e);
            }
        });
    }
}
