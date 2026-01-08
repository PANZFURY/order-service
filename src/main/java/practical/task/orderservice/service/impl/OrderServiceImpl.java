package practical.task.orderservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practical.task.orderservice.dto.request.OrderCreateDto;
import practical.task.orderservice.dto.request.OrderItemCreateDto;
import practical.task.orderservice.dto.response.OrderResponseDto;
import practical.task.orderservice.dto.response.UserInfoDto;
import practical.task.orderservice.mapper.OrderMapper;
import practical.task.orderservice.model.Item;
import practical.task.orderservice.model.Order;
import practical.task.orderservice.model.OrderItem;
import practical.task.orderservice.misc.OrderStatus;
import practical.task.orderservice.repository.ItemRepository;
import practical.task.orderservice.repository.OrderRepository;
import practical.task.orderservice.service.OrderService;
import practical.task.orderservice.specification.OrderSpecifications;
import practical.task.orderservice.service.client.UserClient;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper mapper;
    private final UserClient userClient;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            ItemRepository itemRepository,
                            OrderMapper mapper,
                            UserClient userClient) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.mapper = mapper;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        Order order = mapper.toEntity(dto);
        order.setStatus(OrderStatus.PROCESSING);

        long total = 0;
        for (OrderItemCreateDto itDto : dto.items()) {
            Item item = itemRepository.findById(itDto.itemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + itDto.itemId()));
            OrderItem oi = new OrderItem();
            oi.setItem(item);
            oi.setQuantity(itDto.quantity());
            oi.setOrder(order);
            order.getItems().add(oi);
            total += (long) item.getPrice() * itDto.quantity();
        }
        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        OrderResponseDto responseDto = mapper.toDto(saved, this);
        UserInfoDto user = fetchUserInfoByIdWithCircuitbreaker(saved.getUserId());
        responseDto.setUser(user);

        return responseDto;
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        OrderResponseDto dto = mapper.toDto(order, this);
        dto.setUser(fetchUserInfoByIdWithCircuitbreaker(order.getUserId()));
        return dto;
    }

    @Override
    public Page<OrderResponseDto> getOrders(Instant from, Instant to, List<OrderStatus> statuses, Pageable pageable) {
        var spec = Specification.where(OrderSpecifications.notDeleted())
                .and(OrderSpecifications.createdBetween(from, to))
                .and(OrderSpecifications.hasStatuses(statuses));

        Page<Order> page = orderRepository.findAll(spec, pageable);

        return page.map(order -> mapper.toDto(order, this));
    }

    @Override
    public List<OrderResponseDto> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(o -> {
            var dto = mapper.toDto(o, this);
            dto.setUser(fetchUserInfoByIdWithCircuitbreaker(userId));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderCreateDto updateDto) {
        Order order = orderRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.getItems().clear();
        long total = 0;

        for (OrderItemCreateDto itDto : updateDto.items()) {
            Item item = itemRepository.findById(itDto.itemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + itDto.itemId()));
            OrderItem oi = new OrderItem();
            oi.setItem(item);
            oi.setQuantity(itDto.quantity());
            oi.setOrder(order);
            order.getItems().add(oi);
            total += (long) item.getPrice() * itDto.quantity();
        }
        order.setTotalPrice(total);
        order.setUserId(updateDto.userId());

        Order saved = orderRepository.save(order);
        var dto = mapper.toDto(saved, this);
        dto.setUser(fetchUserInfoByIdWithCircuitbreaker(saved.getUserId()));
        return dto;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.isDeleted()) return;
        order.setDeleted(true);
        orderRepository.save(order);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    public UserInfoDto fetchUserInfoByIdWithCircuitbreaker(Long userId) {
        return userClient.getById(userId);
    }

    public UserInfoDto userFallback(Long userId, Throwable t) {
        UserInfoDto fallback = new UserInfoDto(
                userId,
                "unknown",
                "Unavailable",
                "");

        return fallback;
    }
}
