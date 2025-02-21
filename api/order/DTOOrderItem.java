package order.app.api.order;

import jakarta.validation.constraints.NotNull;

public record DTOOrderItem(Long productId, String productName, Integer quantity, Double unitPrice) {}

