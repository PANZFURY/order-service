package practical.task.orderservice.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import practical.task.orderservice.dto.request.OrderCreateDto;
import practical.task.orderservice.dto.request.OrderItemCreateDto;
import practical.task.orderservice.dto.response.OrderResponseDto;
import practical.task.orderservice.dto.response.UserInfoDto;
import practical.task.orderservice.mapper.OrderMapper;
import practical.task.orderservice.model.Item;
import practical.task.orderservice.model.Order;
import practical.task.orderservice.misc.OrderStatus;
import practical.task.orderservice.repository.ItemRepository;
import practical.task.orderservice.repository.OrderRepository;
import practical.task.orderservice.service.client.UserClient;
import practical.task.orderservice.service.impl.OrderServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item item;
    private Order order;
    private OrderCreateDto orderCreateDto;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("iPhone");
        item.setPrice(500);

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PROCESSING);

        orderCreateDto = new OrderCreateDto(
                1L,
                List.of(new OrderItemCreateDto(1L, 2))
        );

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setUserId(1L);
        orderResponseDto.setStatus(OrderStatus.PROCESSING);
    }

    @Test
    void createOrderSuccess() {
        //given
        when(mapper.toEntity(orderCreateDto)).thenReturn(order);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toDto(order, orderService)).thenReturn(orderResponseDto);
        when(userClient.getById(1L)).thenReturn(new UserInfoDto(1L, "John", "Doe", "email"));

        //when
        OrderResponseDto response = orderService.createOrder(orderCreateDto);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(orderRepository, times(1)).save(order);
        verify(mapper, times(1)).toDto(order, orderService);
    }

    @Test
    void getOrderByIdSuccess() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(mapper.toDto(order, orderService)).thenReturn(orderResponseDto);
        when(userClient.getById(1L)).thenReturn(new UserInfoDto(1L, "John", "Doe", "email"));

        //when
        OrderResponseDto response = orderService.getOrderById(1L);

        //then
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void updateOrderSuccess() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toDto(order, orderService)).thenReturn(orderResponseDto);
        when(userClient.getById(1L)).thenReturn(new UserInfoDto(1L, "John", "Doe", "email"));

        //when
        OrderResponseDto response = orderService.updateOrder(1L, orderCreateDto);

        //then
        assertNotNull(response);
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrderSuccess() {
        //given
        order.setDeleted(false);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        //when
        orderService.deleteOrder(1L);

        //then
        assertTrue(order.isDeleted());
        verify(orderRepository).save(order);
    }

    @Test
    void createOrderItemNotFoundShouldThrowException() {
        //given
        when(mapper.toEntity(orderCreateDto)).thenReturn(order);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        //when+then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.createOrder(orderCreateDto)
        );

        assertTrue(ex.getMessage().contains("Item not found"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderByIdNotFoundShouldThrowException() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        //when + then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrderById(1L)
        );

        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void getOrderByIdDeletedOrderShouldThrowException() {
        //given
        order.setDeleted(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        //when+then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.getOrderById(1L)
        );

        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void updateOrderNotFoundShouldThrowException() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        //when+then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.updateOrder(1L, orderCreateDto)
        );

        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void updateOrderItemNotFoundShouldThrowException() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        //when+then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.updateOrder(1L, orderCreateDto)
        );

        assertTrue(ex.getMessage().contains("Item not found"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrderNotFoundShouldThrowException() {
        //given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        //when+then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> orderService.deleteOrder(1L)
        );

        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void deleteOrderAlreadyDeletedShouldDoNothing() {
        //given
        order.setDeleted(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        //when
        orderService.deleteOrder(1L);

        //then
        verify(orderRepository, never()).save(any());
    }

}