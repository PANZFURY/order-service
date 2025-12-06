package practical.task.orderservice.specification;

import org.springframework.data.jpa.domain.Specification;
import practical.task.orderservice.model.Order;
import practical.task.orderservice.misc.OrderStatus;

import jakarta.persistence.criteria.Path;
import java.time.Instant;
import java.util.List;

public class OrderSpecifications {

    public static Specification<Order> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Order> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            Path<Instant> createdAt = root.get("createdAt");
            if (from != null && to != null) {
                return cb.between(createdAt, from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(createdAt, from);
            } else if (to != null) {
                return cb.lessThanOrEqualTo(createdAt, to);
            } else {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("status").in(statuses);
        };
    }
}

