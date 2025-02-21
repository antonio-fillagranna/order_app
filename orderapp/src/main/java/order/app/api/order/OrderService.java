package order.app.api.order;

import order.app.api.client.Client;
import order.app.api.client.ClientService;
import order.app.api.product.Product;
import order.app.api.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProductService productService;

    public Order createOrder(Order order) {
        Objects.requireNonNull(order, "Pedido não pode ser nulo");
        order.setOrderDate(order.getOrderDate() != null ? order.getOrderDate() : LocalDateTime.now());
        order.setOrderItems(order.getOrderItems() != null ? order.getOrderItems() : Collections.emptyList());
        return orderRepository.save(order);
    }

    public Page<DTOOrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(order -> {
            List<DTOOrderItem> dtoOrderItems = order.getOrderItems().stream()
                    .map(orderItem -> new DTOOrderItem(orderItem.getProduct().getId(),
                            orderItem.getProduct().getName(),
                            orderItem.getQuantity(),
                            orderItem.getProduct().getPrice()))
                    .collect(Collectors.toList());

            Double totalValue = order.getOrderItems().stream()
                    .mapToDouble(orderItem -> orderItem.getProduct().getPrice() * orderItem.getQuantity())
                    .sum();

            return new DTOOrderResponse(order.getId(), order.getClient().getId(), order.getStatus(), dtoOrderItems, totalValue);
        });
    }

    public Order getOrderById(Long id) {
        Objects.requireNonNull(id, "ID do pedido não pode ser nulo");
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    public DTOOrderResponse updateOrder(DTOOrderUpdate dto) {
        Objects.requireNonNull(dto, "DTO de atualização não pode ser nulo");
        Order order = getOrderById(dto.id());

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível alterar um pedido finalizado ou cancelado.");
        }

        order.setStatus(dto.status());
        Order updatedOrder = orderRepository.save(order);

        return getOrderByIdResponse(updatedOrder.getId());
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Objects.requireNonNull(orderId, "ID do pedido não pode ser nulo");
        Order order = getOrderById(orderId);

        if (!order.getOrderItems().isEmpty()) {
            orderItemRepository.deleteAll(order.getOrderItems());
        }
        orderRepository.delete(order);
    }

    public Product getProductById(Long productId) {
        return productService.getProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public Order createOrderFromDto(DTOCreateOrder dto) {
        Client client = clientService.getClientById(dto.getClientId());

        LocalDateTime orderDate = LocalDateTime.now();
        OrderStatus orderStatus = OrderStatus.PENDING;

        Order order = new Order(client, orderDate, orderStatus);
        List<OrderItem> orderItems = new ArrayList<>();

        for (DTOOrderItem dtoItem : dto.getItems()) {
            Product product = productService.getProductById(dtoItem.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
            OrderItem orderItem = new OrderItem(order, product, dtoItem.quantity());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        return createOrder(order);
    }

    public DTOOrderResponse getOrderByIdResponse(Long id) {
        Order order = getOrderById(id);

        List<DTOOrderItem> dtoOrderItems = order.getOrderItems().stream()
                .map(orderItem -> new DTOOrderItem(
                        orderItem.getProduct().getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getQuantity(),
                        orderItem.getProduct().getPrice()))
                .collect(Collectors.toList());

        Double totalValue = order.getOrderItems().stream()
                .mapToDouble(orderItem -> orderItem.getProduct().getPrice() * orderItem.getQuantity())
                .sum();

        return new DTOOrderResponse(order.getId(), order.getClient().getId(), order.getStatus(), dtoOrderItems, totalValue);
    }

    public Double calculateTotalOrderValue(Order order) {
        return order.getOrderItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    public Page<DTOSalesByClient> getSalesByClient(Pageable pageable) {
        List<DTOSalesByClient> sales = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Order::getClient,
                        Collectors.summingDouble(this::calculateTotalOrderValue)))
                .entrySet().stream()
                .map(entry -> new DTOSalesByClient(entry.getKey().getId(), entry.getKey().getName(), entry.getValue()))
                .sorted(Comparator.comparingDouble(DTOSalesByClient::totalSpent).reversed())
                .collect(Collectors.toList());

        return toPage(sales, pageable);
    }

    public Page<DTOSalesByOrder> getSalesByOrder(Pageable pageable) {
        List<DTOSalesByOrder> sales = orderRepository.findAll().stream()
                .map(order -> new DTOSalesByOrder(order.getId(), order.getClient().getId(), order.getClient().getName(), calculateTotalOrderValue(order)))
                .sorted(Comparator.comparingDouble(DTOSalesByOrder::totalValue).reversed())
                .collect(Collectors.toList());

        return toPage(sales, pageable);
    }

    public Page<DTOSalesByProduct> getSalesByProduct(Pageable pageable) {
        List<DTOSalesByProduct> sales = orderRepository.findAll().stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        orderItem -> orderItem.getProduct(),
                        Collectors.summarizingDouble(orderItem -> orderItem.getUnitPrice() * orderItem.getQuantity())
                ))
                .entrySet().stream()
                .map(entry -> new DTOSalesByProduct(entry.getKey().getId(), entry.getKey().getName(),
                        entry.getValue().getCount(), entry.getValue().getSum()))
                .sorted(Comparator.comparingDouble(DTOSalesByProduct::totalRevenue).reversed())
                .collect(Collectors.toList());

        return toPage(sales, pageable);
    }

    private <T>Page<T> toPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }
}
