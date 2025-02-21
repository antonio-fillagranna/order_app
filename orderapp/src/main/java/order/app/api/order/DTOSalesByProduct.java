package order.app.api.order;

public record DTOSalesByProduct(Long productId, String productName, Long totalQuantitySold, Double totalRevenue) {}
