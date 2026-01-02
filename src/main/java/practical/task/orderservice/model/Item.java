package practical.task.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items")
@Data
public class Item extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

}
