package order.app.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import order.app.api.address.Address;

@Table(name = "client")
@Entity(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties({"orders"})
public class Client {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cell;
    private String email;
    private String doc;

    @Embedded
    private Address address;

    public Client(DTOClientRegistry data) {
        this.name = data.name();
        this.cell = data.cell();
        this.email = data.email();
        this.doc = data.doc();
        this.address = new Address(data.address());
    }

    public Client(Long id, String name, String cell) {
        this.id = id;
        this.name = name;
        this.cell = cell;
    }

}
