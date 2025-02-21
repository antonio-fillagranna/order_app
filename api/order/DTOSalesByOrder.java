package order.app.api.order;

public record DTOSalesByOrder(Long orderId, Long clientId, String clientName, Double totalValue) {}
