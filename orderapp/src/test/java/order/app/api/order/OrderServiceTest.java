package order.app.api.order;

import order.app.api.client.Client;
import order.app.api.client.ClientService;
import order.app.api.product.Product;
import order.app.api.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private ProductService productService;

    private Client client;
    private Client client2;
    private Product product;
    private Order order;
    private Order order2;
    private Order order3;
    private OrderItem orderItem;
    private OrderItem orderItem2;
    private OrderItem orderItem3;
    private DTOCreateOrder dtoCreateOrder;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setName("Client 1");

        client2 = new Client();
        client2.setId(2L);
        client2.setName("Client 2");

        product = new Product();
        product.setId(1L);
        product.setName("Product Name");
        product.setPrice(100.0);

        order = new Order();
        order.setId(1L);
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        order2 = new Order();
        order2.setId(2L);
        order2.setClient(client);
        order2.setOrderDate(LocalDateTime.now());
        order2.setStatus(OrderStatus.PENDING);

        order3 = new Order();
        order3.setId(3L);
        order3.setClient(client2);
        order3.setOrderDate(LocalDateTime.now());
        order3.setStatus(OrderStatus.PENDING);

        orderItem = new OrderItem(order, product, 2, 100.0);
        orderItem2 = new OrderItem(order2, product, 3, 100.0);
        orderItem3 = new OrderItem(order3, product, 1, 100.0);

        order.setOrderItems(Arrays.asList(orderItem));
        order2.setOrderItems(Arrays.asList(orderItem2));
        order3.setOrderItems(Arrays.asList(orderItem3));

        DTOOrderItem dtoOrderItem = new DTOOrderItem(1L, "Product Name", 2, 100.0);
        dtoCreateOrder = new DTOCreateOrder();
        dtoCreateOrder.setClientId(1L);
        dtoCreateOrder.setItems(Arrays.asList(dtoOrderItem));
    }

    @Test
    void testCreateOrder() {
        when(orderRepository.save(order)).thenReturn(order);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(1L, createdOrder.getId());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testCreateOrder_WithDateAndItems() {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.of(2024, 2, 20, 12, 0));
        order.setOrderItems(List.of(new OrderItem()));

        when(orderRepository.save(order)).thenReturn(order);

        Order savedOrder = orderService.createOrder(order);

        assertNotNull(savedOrder.getOrderDate());
        assertFalse(savedOrder.getOrderItems().isEmpty());
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrder_WithoutDateAndItems() {
        Order order = new Order();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            assertNotNull(savedOrder.getOrderDate());
            assertNotNull(savedOrder.getOrderItems());
            return savedOrder;
        });

        orderService.createOrder(order);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetProductById_ProductNotFound() {
        Long productId = 1L;
        when(productService.findById(productId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.getProductById(productId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Produto não encontrado", exception.getReason());
    }

    @Test
    void testGetAllOrders() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setClient(new Client());
        order.setOrderItems(new ArrayList<>());

        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(orderPage);

        Page<DTOOrderResponse> result = orderService.getAllOrders(Pageable.unpaged());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetOrderById_OrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.getOrderById(1L);

        assertNotNull(foundOrder);
        assertEquals(1L, foundOrder.getId());
    }

    @Test
    void testGetOrderById_OrderNotFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.getOrderById(2L));
    }

    @Test
    void testUpdateOrder_FailedForCompletedOrder() {
        Long orderId = 1L;
        DTOOrderUpdate dto = new DTOOrderUpdate(orderId, OrderStatus.PROCESSING);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrder(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Não é possível alterar um pedido finalizado ou cancelado.", exception.getReason());
    }

    @Test
    void testUpdateOrder_FailedForCompleted() {
        DTOOrderUpdate dto = new DTOOrderUpdate(1L, OrderStatus.PROCESSING);
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrder(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Não é possível alterar um pedido finalizado ou cancelado.", exception.getReason());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrder_FailedForCanceled() {
        DTOOrderUpdate dto = new DTOOrderUpdate(1L, OrderStatus.PROCESSING);
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.updateOrder(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Não é possível alterar um pedido finalizado ou cancelado.", exception.getReason());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Success() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderItems(List.of(new OrderItem()));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(orderId);

        verify(orderItemRepository).deleteAll(order.getOrderItems());
        verify(orderRepository).delete(order);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.cancelOrder(orderId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pedido não encontrado", exception.getReason());
    }

    @Test
    void testCancelOrder_NullOrderId() {
        Long orderId = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> orderService.cancelOrder(orderId));

        assertEquals("ID do pedido não pode ser nulo", exception.getMessage());
    }

    @Test
    void testCancelOrder_EmptyOrderItems() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderItems(Collections.emptyList());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(orderId);

        verify(orderItemRepository, never()).deleteAll(anyList());
        verify(orderRepository).delete(order);
    }

    @Test
    void createOrderFromDto_ShouldCreateOrder_WhenDataIsValid() {
        when(clientService.getClientById(1L)).thenReturn(client);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrderFromDto(dtoCreateOrder);

        assertEquals(client, order.getClient());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(product, order.getOrderItems().get(0).getProduct());
        assertEquals(2, order.getOrderItems().get(0).getQuantity());

        verify(clientService).getClientById(1L);
        verify(productService).getProductById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrderFromDto_ShouldThrowException_WhenProductNotFound() {
        when(clientService.getClientById(1L)).thenReturn(client);
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> orderService.createOrderFromDto(dtoCreateOrder));
    }

    @Test
    void getOrderByIdResponse_ShouldReturnDTOOrderResponse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        DTOOrderResponse dtoOrderResponse = orderService.getOrderByIdResponse(1L);

        assertEquals(order.getId(), dtoOrderResponse.orderId());
        assertEquals(client.getId(), dtoOrderResponse.clientId());
        assertEquals(order.getStatus(), dtoOrderResponse.status());
        assertEquals(1, dtoOrderResponse.items().size());
        assertEquals(orderItem.getProduct().getId(), dtoOrderResponse.items().get(0).productId());
        assertEquals(orderItem.getProduct().getName(), dtoOrderResponse.items().get(0).productName());
        assertEquals(orderItem.getQuantity(), dtoOrderResponse.items().get(0).quantity());
        assertEquals(orderItem.getProduct().getPrice(), dtoOrderResponse.items().get(0).unitPrice());
        assertEquals(200.0, dtoOrderResponse.totalValue());

        verify(orderRepository).findById(1L);
    }

    @Test
    void calculateTotalOrderValue_ShouldReturnCorrectTotalValue() {
        Double totalValue = orderService.calculateTotalOrderValue(order);

        assertEquals(200.0, totalValue);
    }

}