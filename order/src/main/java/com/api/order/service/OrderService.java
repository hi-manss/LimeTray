package com.api.order.service;

import com.api.order.dto.req.OrderRequest;
import com.api.order.dto.res.OrderResponse;
import com.api.order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface OrderService {
    public OrderResponse placeOrder(OrderRequest request);
    public Orders getOrderById(Long id);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Orders findByIdEntity(Long id);
    void updateOrder(Map<String,String> param);
}
