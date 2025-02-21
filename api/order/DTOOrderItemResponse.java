package order.app.api.order;

public record DTOOrderItemResponse(
        Long productId,
        String productName,
        int quantity
) {
    public DTOOrderItemResponse(OrderItem orderItem) {
        this(orderItem.getProduct().getId(), orderItem.getProduct().getName(), orderItem.getQuantity());
    }
}

