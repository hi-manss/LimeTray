package com.api.order.service;
import com.api.order.entity.Orders;
import com.api.order.enums.OrderStatus;
import org.springframework.stereotype.Service;


@Service
public interface OrderServiceDetailService {

     void saveOrderStatusLogs(Orders order, OrderStatus status, String updatedBy, String notes);

}
