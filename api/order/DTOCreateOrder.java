package order.app.api.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DTOCreateOrder {

    @NotNull
    private Long clientId;

    @NotNull
    private List<DTOOrderItem> items;

}
