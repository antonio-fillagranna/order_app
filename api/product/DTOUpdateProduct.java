package order.app.api.product;

import lombok.Data;

@Data
public class DTOUpdateProduct {
    private Double price;
    private String description;
    private ProductCategory category;
}
