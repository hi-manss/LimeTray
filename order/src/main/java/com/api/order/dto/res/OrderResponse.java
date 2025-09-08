package com.api.order.dto.res;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        List<ProductResponse> items,
        BigDecimal totalAmount,
        OffsetDateTime orderTime,
        String status
) {}