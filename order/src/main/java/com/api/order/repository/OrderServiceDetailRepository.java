package com.api.order.repository;

import com.api.order.entity.OrderStatusDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderServiceDetailRepository extends JpaRepository<OrderStatusDetails, Long> {
}
