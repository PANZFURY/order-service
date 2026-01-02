package practical.task.orderservice.specification;

import org.springframework.data.jpa.domain.Specification;
import practical.task.orderservice.model.Order;
import practical.task.orderservice.misc.OrderStatus;

import java.time.Instant;
import java.util.List;
public class OrderSpecification {

    public static Specification<Order> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Order> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if(from == null && to == null) return cb.conjunction();
            if(from == null) return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            if(to == null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.between(root.get("createdAt"), from, to);
        };
    }

    public static Specification<Order> statusIn(List<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if(statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Order> userIdEquals(Long userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction() : cb.equal(root.get("userId"), userId);
    }
}