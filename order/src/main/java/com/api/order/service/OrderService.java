package com.api.order.service;

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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

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

    public OrderResponse getOrderById(Long id) {
        try {
            log.info("Fetching order with id: {}", id);

            Orders order = ordersRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Order not found with id: " + id, "ORDER_NOT_FOUND"));

            log.info("Order {} retrieved successfully", id);

            return mapToOrderResponse(order);

        } catch (ApiException ex) {
            log.warn("Workflow error while fetching order {}: {}", id, ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while fetching order {}", id, ex);
            throw new ApiException("Failed to fetch order. Please try again later.", "ORDER_FETCH_FAILED");
        }
    }


}
