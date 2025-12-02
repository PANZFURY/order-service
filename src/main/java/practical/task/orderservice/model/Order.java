package practical.task.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import practical.task.orderservice.misc.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long user_id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Long total_price;

    private boolean deleted;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

}
