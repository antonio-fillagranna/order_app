package order.app.api.order;

public record DTOCreateOrderItem(
        Long productId,
        int quantity
) {}