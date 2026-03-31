package practical.task.orderservice.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import practical.task.orderservice.dto.request.OrderCreateDto;
import practical.task.orderservice.dto.response.OrderResponseDto;
import practical.task.orderservice.misc.OrderStatus;
import practical.task.orderservice.service.OrderService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    @Autowired
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderCreateDto dto) {
        OrderResponseDto created = service.createOrder(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAll(
            @RequestParam(required = false) Long fromEpochMillis,
            @RequestParam(required = false) Long toEpochMillis,
            @RequestParam(required = false) String statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Instant from = fromEpochMillis != null ? Instant.ofEpochMilli(fromEpochMillis) : null;
        Instant to = toEpochMillis != null ? Instant.ofEpochMilli(toEpochMillis) : null;
        List<OrderStatus> statusList = null;
        if (statuses != null && !statuses.isBlank()) {
            statusList = Arrays.stream(statuses.split(","))
                    .map(String::trim)
                    .map(OrderStatus::valueOf)
                    .collect(Collectors.toList());
        }
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getOrders(from, to, statusList, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getOrdersByUserId(userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDto> update(@PathVariable Long id, @Valid @RequestBody OrderCreateDto dto) {
        return ResponseEntity.ok(service.updateOrder(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
