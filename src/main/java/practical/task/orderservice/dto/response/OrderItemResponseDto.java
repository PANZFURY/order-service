package practical.task.orderservice.dto.response;

public record OrderItemResponseDto(
        Long id,
        Long itemId,
        String itemName,
        Integer itemPrice,
        Integer quantity
) {
}
