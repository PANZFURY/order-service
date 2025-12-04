package practical.task.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practical.task.orderservice.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
