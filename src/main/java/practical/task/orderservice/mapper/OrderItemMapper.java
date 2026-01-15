package practical.task.orderservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import practical.task.orderservice.dto.response.OrderItemResponseDto;
import practical.task.orderservice.model.OrderItem;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemResponseDto toDto(OrderItem orderItem);
}
