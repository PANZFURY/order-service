package practical.task.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practical.task.orderservice.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {}
