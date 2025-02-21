package order.app.api.address;

import jakarta.validation.constraints.NotBlank;

public record DTOAddress(
        @NotBlank
        String street,
        @NotBlank
        String number,
        @NotBlank
        String postalcode,

        String neighborhood,
        @NotBlank
        String city,
        @NotBlank
        String state,
        @NotBlank
        String country,

        String additionalinfo
        ) {
}
