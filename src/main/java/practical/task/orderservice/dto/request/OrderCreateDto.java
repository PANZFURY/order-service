package practical.task.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateDto (
        @NotNull
        Long userId,
        List<OrderItemCreateDto> items
) {
}