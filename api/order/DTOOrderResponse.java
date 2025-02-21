package order.app.api.order;

import java.util.List;
import java.util.stream.Collectors;

public record DTOOrderResponse(Long orderId, Long clientId, OrderStatus status, List<DTOOrderItem> items, Double totalValue) {}