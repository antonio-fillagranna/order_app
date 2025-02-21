package order.app.api.order;

import java.util.List;

public record DTOOrder(Long orderId, Long clientId, OrderStatus status, List<DTOOrderItem> items) {}
