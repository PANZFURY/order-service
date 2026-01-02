package practical.task.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    private Integer quantity;

}
