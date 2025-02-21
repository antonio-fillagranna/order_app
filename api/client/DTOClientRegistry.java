package order.app.api.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import order.app.api.address.DTOAddress;

public record DTOClientRegistry(
        @NotBlank
        String name,
        @NotBlank
        String cell,
        @NotBlank
                @Email
        String email,
        @NotBlank
                @Pattern(regexp = "^[a-zA-Z0-9]{1,20}$")
        String doc,
        @NotNull
                @Valid
        DTOAddress address
        ) {
}
