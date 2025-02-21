package order.app.api.client;

import order.app.api.address.Address;
import order.app.api.address.DTOAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new Client(1L, "John Doe", "987654321", "john.doe@example.com", "123456789", new Address("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));
    }

    @Test
    void findById_ShouldReturnClient_WhenClientExists() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        Client foundClient = clientService.findById(1L);

        assertEquals(client, foundClient);
        verify(clientRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> clientService.findById(1L));
    }

    @Test
    void testUpdateInfo_SuccessfulUpdate() {
        Long clientId = 1L;
        DTOUpdateClient dto = new DTOUpdateClient(clientId, "Jane Doe", "123456789", "jane.doe@example.com", new DTOAddress("New Street", "2", "98765-432", "New Neighborhood", "New City", "New State", "New Country", "New Additional Info"));
        Client existingClient = new Client(clientId, "John Doe", "987654321", "john.doe@example.com", "123456789", new Address("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Client updatedClient = clientService.updateInfo(clientId, dto);

        assertNotNull(updatedClient);
        assertEquals("Jane Doe", updatedClient.getName());
        assertEquals("123456789", updatedClient.getCell());
        assertEquals("jane.doe@example.com", updatedClient.getEmail());
        assertEquals("123456789", updatedClient.getDoc());
        assertNotNull(updatedClient.getAddress());
        assertEquals("Street", updatedClient.getAddress().getStreet());
        assertEquals("1", updatedClient.getAddress().getNumber());
        assertEquals("12345-678", updatedClient.getAddress().getPostalcode());
        assertEquals("Neighborhood", updatedClient.getAddress().getNeighborhood());
        assertEquals("City", updatedClient.getAddress().getCity());
        assertEquals("State", updatedClient.getAddress().getState());
        assertEquals("Country", updatedClient.getAddress().getCountry());
        assertEquals("Additional Info", updatedClient.getAddress().getAdditionalinfo());
        verify(clientRepository, times(1)).save(existingClient);
    }

    @Test
    void testUpdateInfo_OnlyNameUpdated() {
        Long clientId = 1L;
        DTOUpdateClient dto = new DTOUpdateClient(clientId, "Jane Doe", null, null, null);
        Client existingClient = new Client(clientId, "John Doe", "987654321", "john.doe@example.com", "123456789", new Address("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Client updatedClient = clientService.updateInfo(clientId, dto);

        assertNotNull(updatedClient);
        assertEquals("Jane Doe", updatedClient.getName());
        assertEquals("987654321", updatedClient.getCell());
        assertEquals("john.doe@example.com", updatedClient.getEmail());
    }

    @Test
    void testUpdateInfo_OnlyCellUpdated() {
        Long clientId = 1L;
        DTOUpdateClient dto = new DTOUpdateClient(clientId, null, "123456789", null, null);
        Client existingClient = new Client(clientId, "John Doe", "987654321", "john.doe@example.com", "123456789", new Address("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Client updatedClient = clientService.updateInfo(clientId, dto);

        assertNotNull(updatedClient);
        assertEquals("John Doe", updatedClient.getName());
        assertEquals("123456789", updatedClient.getCell());
        assertEquals("john.doe@example.com", updatedClient.getEmail());
    }

    @Test
    void testUpdateInfo_OnlyEmailUpdated() {
        Long clientId = 1L;
        DTOUpdateClient dto = new DTOUpdateClient(clientId, null, null, "new.email@example.com", null);
        Client existingClient = new Client(clientId, "John Doe", "987654321", "john.doe@example.com", "123456789", new Address("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        Client updatedClient = clientService.updateInfo(clientId, dto);

        assertNotNull(updatedClient);
        assertEquals("John Doe", updatedClient.getName());
        assertEquals("987654321", updatedClient.getCell());
        assertEquals("new.email@example.com", updatedClient.getEmail());
    }

    @Test
    void testUpdateInfo_ClientNotFound() {
        Long clientId = 1L;
        DTOUpdateClient dto = new DTOUpdateClient(clientId, "Jane Doe", "123456789", "jane.doe@example.com", null);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clientService.updateInfo(clientId, dto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testListClients() {
        Pageable pageable = PageRequest.of(0, 10);

        Address address1 = new Address("Rua A", "123", "12345-678", "Centro", "São Paulo", "SP", "Brasil", "Apto 1");
        Address address2 = new Address("Rua B", "456", "98765-432", "Bairro B", "Rio de Janeiro", "RJ", "Brasil", "Casa 2");

        Client client1 = new Client(1L, "John Doe", "123456789", "john@example.com", "123.456.789-00", address1);
        Client client2 = new Client(2L, "Jane Doe", "987654321", "jane@example.com", "987.654.321-00", address2);

        Page<Client> clientPage = new PageImpl<>(List.of(client1, client2));

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);

        Page<DTOClientResponse> result = clientService.listClients(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(clientRepository).findAll(pageable);
    }

    @Test
    void testRegisterClient() {
        DTOClientRegistry dto = new DTOClientRegistry("Jane Doe", "123456789", "jane.doe@example.com", "987654321", new DTOAddress("Street", "1", "12345-678", "Neighborhood", "City", "State", "Country", "Additional Info"));
        Client newClient = new Client(dto);

        when(clientRepository.save(any(Client.class))).thenReturn(newClient);

        Client registeredClient = clientService.registerClient(dto);

        assertNotNull(registeredClient);
        assertEquals("Jane Doe", registeredClient.getName());
        assertEquals("123456789", registeredClient.getCell());
        assertEquals("jane.doe@example.com", registeredClient.getEmail());
        assertNotNull(registeredClient.getAddress());
        assertEquals("Street", registeredClient.getAddress().getStreet());
        assertEquals("1", registeredClient.getAddress().getNumber());
        assertEquals("12345-678", registeredClient.getAddress().getPostalcode());
        assertEquals("Neighborhood", registeredClient.getAddress().getNeighborhood());
        assertEquals("City", registeredClient.getAddress().getCity());
        assertEquals("State", registeredClient.getAddress().getState());
        assertEquals("Country", registeredClient.getAddress().getCountry());
        assertEquals("Additional Info", registeredClient.getAddress().getAdditionalinfo());
        verify(clientRepository, times(1)).save(newClient);
    }

    @Test
    void testDeleteClient_Success() {
        Long clientId = 1L;
        when(clientRepository.existsById(clientId)).thenReturn(true);

        clientService.deleteClient(clientId);

        verify(clientRepository, times(1)).deleteById(clientId);
    }

    @Test
    void testDeleteClient_ClientNotFound() {
        Long clientId = 1L;
        when(clientRepository.existsById(clientId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            clientService.deleteClient(clientId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}