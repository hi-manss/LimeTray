package com.api.order.dto.res;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        Integer qty,
        BigDecimal price
) {}