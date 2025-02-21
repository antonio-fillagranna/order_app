package order.app.api.client;

public record DTOClientResponse(Long id, String name, String cell, String street, String number, String postalcode, String neighborhood, String additionalinfo) {

    public DTOClientResponse(Client client) {
        this(client.getId(), client.getName(), client.getCell(), client.getAddress().getStreet(), client.getAddress().getNumber(), client.getAddress().getPostalcode(),
                client.getAddress().getNeighborhood(), client.getAddress().getAdditionalinfo());
    }
}
