package order.app.api.client;

import jakarta.validation.constraints.NotNull;
import order.app.api.address.DTOAddress;

public record DTOUpdateClient(
        @NotNull
        Long id,
        String name,
        String cell,
        String email,
        DTOAddress address
    ) {
}
