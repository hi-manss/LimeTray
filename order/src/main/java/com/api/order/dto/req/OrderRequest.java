package com.api.order.dto.req;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import jakarta.validation.constraints.*;

public record OrderRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotEmpty(message = "At least one product must be provided")
        List<ProductRequest> items,

        @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than zero")
        BigDecimal totalAmount,

        @NotNull(message = "Order time is required")
        OffsetDateTime orderTime) {
}
