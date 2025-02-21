package order.app.api.order;

public record DTOSalesByClient(Long clientId, String clientName, Double totalSpent) {}
