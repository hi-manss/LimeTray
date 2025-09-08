package com.api.order.repository;

import com.api.order.entity.Orders;
import com.api.order.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
