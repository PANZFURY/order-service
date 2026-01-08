package practical.task.orderservice.mapper;

import org.mapstruct.*;
import practical.task.orderservice.dto.request.OrderCreateDto;
import practical.task.orderservice.dto.response.OrderResponseDto;
import practical.task.orderservice.model.Order;
import practical.task.orderservice.service.impl.OrderServiceImpl;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    Order toEntity(OrderCreateDto dto);

    @Mapping(source = "items", target = "items")
    @Mapping(target = "userId", source = "userId")
    OrderResponseDto toDto(Order order, @Context OrderServiceImpl orderService);

    @AfterMapping
    default void fillUser(Order order, @MappingTarget OrderResponseDto dto, @Context OrderServiceImpl orderService) {
        dto.setUser(orderService.fetchUserInfoByIdWithCircuitbreaker(order.getUserId()));
    }

}