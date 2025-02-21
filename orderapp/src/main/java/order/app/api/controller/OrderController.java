package order.app.api.controller;

import jakarta.validation.Valid;
import order.app.api.order.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody @Valid DTOCreateOrder dto) {
        Order order = orderService.createOrderFromDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId());
    }

    @GetMapping
    public ResponseEntity<Page<DTOOrderResponse>> getAllOrders(
            @PageableDefault(size = 10, sort = {"orderDate"}) Pageable pageable) {
        Page<DTOOrderResponse> orders = orderService.getAllOrders(pageable); // Corrigido para DTOOrderResponse
        return orders.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTOOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdResponse(id));
    }

    @PutMapping
    public ResponseEntity<DTOOrderResponse> updateOrder(@RequestBody DTOOrderUpdate dto) {
        return ResponseEntity.ok(orderService.updateOrder(dto));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sales-by-client")
    public ResponseEntity<Page<DTOSalesByClient>> getSalesByClient(Pageable pageable) {
        return ResponseEntity.ok(orderService.getSalesByClient(pageable));
    }

    @GetMapping("/sales-by-order")
    public ResponseEntity<Page<DTOSalesByOrder>> getSalesByOrder(Pageable pageable) {
        return ResponseEntity.ok(orderService.getSalesByOrder(pageable));
    }

    @GetMapping("/sales-by-product")
    public ResponseEntity<Page<DTOSalesByProduct>> getSalesByProduct(Pageable pageable) {
        return ResponseEntity.ok(orderService.getSalesByProduct(pageable));
    }
}

