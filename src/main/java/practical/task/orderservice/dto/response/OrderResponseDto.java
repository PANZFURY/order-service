package practical.task.orderservice.dto.response;

import lombok.Data;
import practical.task.orderservice.misc.OrderStatus;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private Long totalPrice;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponseDto> items;
    private UserInfoDto user;
}
