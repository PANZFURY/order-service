package practical.task.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemCreateDto (
        @NotNull
        Long itemId,

        @NotNull
        @Positive
        Integer quantity
) {
}
