package com.api.order.service.impl;

import com.api.order.dto.req.OrderRequest;
import com.api.order.dto.res.OrderResponse;
import com.api.order.dto.res.ProductResponse;
import com.api.order.entity.Orders;
import com.api.order.entity.Product;
import com.api.order.enums.OrderStatus;
import com.api.order.exceptions.ApiException;
import com.api.order.mq.OrderProducer;
import com.api.order.repository.OrderRepository;
import com.api.order.repository.ProductRepository;
import com.api.order.service.OrderService;
import com.api.order.service.OrderServiceDetailService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component(value = "orderService")
public class OrderServiceImpl implements OrderService {


    private final OrderRepository ordersRepository;
    private final ProductRepository productRepository;
    private final OrderProducer orderProducer;
    private final OrderServiceDetailService orderServiceDetailService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        try {
            log.info("Placing new order for customer: {}", request.customerName());

            // Validation
            if (request.items() == null || request.items().isEmpty()) {
                throw new ApiException("Order must contain at least one product", "EMPTY_ORDER");
            }

            if (request.totalAmount() == null || request.totalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Total amount must be greater than zero", "INVALID_AMOUNT");
            }

            // Create Order
            Orders order = new Orders();
            order.setCustomerName(request.customerName());
            order.setTotalAmount(request.totalAmount());
            order.setOrderTime(request.orderTime());
            order.setStatus(OrderStatus.CREATED);

            Orders savedOrder = ordersRepository.save(order);

            // Map products
            List<Product> products = request.items().stream()
                    .map(p -> {
                        Product product = new Product();
                        product.setName(p.name());
                        product.setQuantity(p.quantity());
                        product.setPrice(p.price());
                        product.setOrders(savedOrder);
                        return product;
                    })
                    .collect(Collectors.toList());

            productRepository.saveAll(products);
            savedOrder.setItems(products);

            log.info("Order {} placed successfully with {} items", savedOrder.getId(), products.size());

            orderServiceDetailService.saveOrderStatusLogs(savedOrder,OrderStatus.CREATED,"User","Order created");
            // Push order to Kafka queue for async processing
            orderProducer.sendOrder(savedOrder.getId());
            log.info("Order {} added to processing queue", savedOrder.getId());

            return mapToOrderResponse(savedOrder);

        } catch (ApiException ex) {
            log.error("Workflow error while placing order: {}", ex.getMessage(), ex);
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while placing order", ex);
            throw new ApiException("Failed to place order. Please try again later.", "ORDER_CREATION_FAILED");
        }
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return ordersRepository.findAll(pageable)
                .map(this::mapToOrderResponse);
    }
    public Orders getOrderById(Long id) {
        try {
            log.info("Fetching order with id: {}", id);

            Orders order = ordersRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Order not found with id: " + id, "ORDER_NOT_FOUND"));

            log.info("Order {} retrieved successfully", id);

            return order;

        } catch (ApiException ex) {
            log.warn("Workflow error while fetching order {}: {}", id, ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while fetching order {}", id, ex);
            throw new ApiException("Failed to fetch order. Please try again later.", "ORDER_FETCH_FAILED");
        }
    }


    private OrderResponse mapToOrderResponse(Orders order) {
        List<ProductResponse> products = order.getItems().stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getQuantity(),
                        p.getPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                products,
                order.getTotalAmount(),
                order.getOrderTime(),
                order.getStatus().toString()
        );
    }


    public Orders findByIdEntity(Long id) {
        return ordersRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateOrder(Map<String, String> params) {

        Long id;
        try {
            id = Long.parseLong(params.get("id"));
        } catch (NumberFormatException e) {
            throw new ApiException("Invalid order ID format", "INVALID_ID_FORMAT");
        }

        if (params.size() == 1 && params.containsKey("id")) {
            throw new ApiException("No update fields provided other than ID", "NO_UPDATE_FIELDS");
        }

        Orders existingOrder = findByIdEntity(id);
        if (existingOrder == null) {
            throw new ApiException("Order not found with ID: " + id, "ORDER_NOT_FOUND");
        }

        boolean updated = false;
        OrderStatus oldStatus = existingOrder.getStatus();

        if (params.containsKey("customerName")) {
            existingOrder.setCustomerName(params.get("customerName"));
            updated = true;
        }

        if (params.containsKey("status")) {
            try {
                OrderStatus newStatus = OrderStatus.valueOf(params.get("status").toUpperCase());
                existingOrder.setStatus(newStatus);
                updated = true;
            } catch (IllegalArgumentException e) {
                throw new ApiException("Invalid status value", "INVALID_STATUS");
            }
        }

        if (params.containsKey("totalAmount")) {
            try {
                existingOrder.setTotalAmount(new BigDecimal(params.get("totalAmount")));
                updated = true;
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid totalAmount format", "INVALID_AMOUNT");
            }
        }

        if (!updated) {
            throw new ApiException("No valid update fields provided", "NO_VALID_UPDATE_FIELDS");
        }

        existingOrder.setUpdatedAt(Timestamp.from(Instant.now()));

        this.ordersRepository.save(existingOrder);
        log.info("Order {} updated successfully", id);

        if (params.containsKey("status") && !existingOrder.getStatus().equals(oldStatus)) {
            orderServiceDetailService.saveOrderStatusLogs(
                    existingOrder,
                    existingOrder.getStatus(),
                    "User",
                    "Order status updated via API"
            );
        }
    }


}
