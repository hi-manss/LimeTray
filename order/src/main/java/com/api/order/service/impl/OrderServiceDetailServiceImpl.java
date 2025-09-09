package com.api.order.service.impl;

import com.api.order.entity.OrderStatusDetails;
import com.api.order.entity.Orders;
import com.api.order.enums.OrderStatus;
import com.api.order.repository.OrderServiceDetailRepository;
import com.api.order.service.OrderServiceDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@RequiredArgsConstructor
@Slf4j
@Component(value = "orderServiceDetail")
public class OrderServiceDetailServiceImpl implements OrderServiceDetailService {
    private final OrderServiceDetailRepository orderStatusDetailsRepository;

    public void saveOrderStatusLogs(Orders order, OrderStatus status, String updatedBy, String notes) {
        OrderStatusDetails details = OrderStatusDetails.builder()
                .order(order)
                .status(status)
                .updatedBy(updatedBy)
                .notes(notes)
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        orderStatusDetailsRepository.save(details);
        log.info("Order status log saved: OrderId={}, Status={}", order.getId(), status);
    }
}
