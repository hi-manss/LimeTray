package com.api.order.entity;
import com.api.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private String customerName;

    @OneToMany
    @JoinColumn(name = "order_fk_id")
    @JsonManagedReference
    private List<Product> items;

    @Column( precision = 13, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column( nullable = false)
    private OffsetDateTime orderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OrderStatus status;

    @Column( updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column( columnDefinition = "TIMESTAMP")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "order")
    @JsonBackReference
    private List<OrderStatusDetails> orderStatusDetails;

    private String notes;

}
