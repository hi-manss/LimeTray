package com.api.order.service;


import com.api.order.entity.OrderStatusDetails;
import com.api.order.entity.Orders;
import com.api.order.enums.OrderStatus;
import com.api.order.repository.OrderServiceDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public interface OrderServiceDetailService {

    public void saveOrderStatusLogs(Orders order, OrderStatus status, String updatedBy, String notes);

}
