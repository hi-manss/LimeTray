package com.api.order.controller;

import com.api.order.dto.req.OrderRequest;
import com.api.order.dto.res.OrderResponse;
import com.api.order.entity.Orders;
import com.api.order.enums.OrderStatus;
import com.api.order.exceptions.ApiException;
import com.api.order.service.OrderService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1")
@Slf4j
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
        } catch (Exception ex) {
            log.error("Error fetching orders with pagination", ex);
            throw new ApiException("Failed to fetch paginated orders", "ORDER_PAGINATION_FAILED");
        }
    }

    @GetMapping(value = "/order/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));

    }

    @PostMapping(value = "/order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));

    }

    @PatchMapping(value = "/order")
    @Transactional
    public ResponseEntity<?> updateOrder(@RequestParam Map<String, String> params) {
        if (params.isEmpty()) {
            throw new ApiException("No parameters provided for update", "NO_UPDATE_PARAMS");
        }
        orderService.updateOrder(params);

        return ResponseEntity.ok("Order updated successfully");
    }

}
