package order.app.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @Transactional
    public Client registerClient(DTOClientRegistry data) {
        Client client = new Client(data);
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<DTOClientResponse> listClients(Pageable pageable) {
        return clientRepository.findAll(pageable).map(DTOClientResponse::new);
    }

    @Transactional
    public Client updateInfo(Long id, DTOUpdateClient dto) {
        Client client = getClientById(id); // Evita código duplicado

        if (dto.name() != null && !dto.name().isBlank()) {
            client.setName(dto.name());
        }
        if (dto.email() != null && !dto.email().isBlank()) {
            client.setEmail(dto.email());
        }
        if (dto.cell() != null && !dto.cell().isBlank()) {
            client.setCell(dto.cell());
        }

        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
        }
        clientRepository.deleteById(id);
    }
}
