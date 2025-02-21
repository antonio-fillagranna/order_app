package order.app.api.address;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street;
    private String number;
    private String postalcode;
    private String neighborhood;
    private String city;
    private String state;
    private String country;
    private String additionalinfo;

    public Address(DTOAddress address) {
        this.street = address.street();
        this.number = address.number();
        this.postalcode = address.postalcode();
        this.neighborhood = address.neighborhood();
        this.city = address.city();
        this.state = address.state();
        this.country = address.country();
        this.additionalinfo = address.additionalinfo();
    }

    public void updateInfo(DTOAddress data) {
        if (data.street() != null) {
            this.street = data.street();
        }
        if (data.number() != null) {
            this.number = data.street();
        }
        if (data.postalcode() != null) {
            this.postalcode = data.street();
        }
        if (data.neighborhood() != null) {
            this.neighborhood = data.street();
        }
        if (data.city() != null) {
            this.city = data.street();
        }
        if (data.state() != null) {
            this.state = data.street();
        }
        if (data.country() != null) {
            this.country = data.street();
        }
        if (data.additionalinfo() != null) {
            this.additionalinfo = data.street();
        }
    }
}