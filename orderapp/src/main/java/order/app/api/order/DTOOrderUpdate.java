package order.app.api.order;

import jakarta.validation.constraints.NotNull;

public record DTOOrderUpdate(
        @NotNull Long id,
        @NotNull OrderStatus status
) {}
