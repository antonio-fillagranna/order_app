package order.app.api.product;

public record DTOProductRegistry(
        String name,
        Double price,
        String description,
        ProductCategory category) {
}
