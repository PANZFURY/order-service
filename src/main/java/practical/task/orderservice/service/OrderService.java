package practical.task.orderservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import practical.task.orderservice.dto.request.OrderCreateDto;
import practical.task.orderservice.dto.response.OrderResponseDto;
import practical.task.orderservice.misc.OrderStatus;

import java.time.Instant;
import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(OrderCreateDto dto);
    OrderResponseDto getOrderById(Long id);
    Page<OrderResponseDto> getOrders(Instant from, Instant to, List<OrderStatus> statuses, Pageable pageable);
    List<OrderResponseDto> getOrdersByUserId(Long userId);
    OrderResponseDto updateOrder(Long id, OrderCreateDto updateDto);
    void deleteOrder(Long id);
}
