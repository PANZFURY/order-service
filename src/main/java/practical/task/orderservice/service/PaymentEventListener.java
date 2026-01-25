package practical.task.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import practical.task.common.event.PaymentCreatedEvent;
import practical.task.orderservice.misc.OrderStatus;

@Component
public class PaymentEventListener {

    private final OrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);


    @Autowired
    public PaymentEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "create-payment", groupId = "order-service")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("Received Kafka event: orderId={}, paymentId={}, status={}",
                event.getOrderId(), event.getPaymentId(), event.getStatus());
        if(event.getOrderId() == null){
            log.warn("Received event with null orderId, skipping");
            return;
        }
        log.info(event.getOrderId());
        Long id = Long.valueOf(event.getOrderId());
        log.info("Sd " + id);
        orderService.updateOrderStatus(
                id,
                mapPaymentStatusToOrderStatus(event.getStatus())
        );
    }

    private OrderStatus mapPaymentStatusToOrderStatus(String paymentStatus) {
        return switch (paymentStatus) {
            case "SUCCESS" -> OrderStatus.DELIVERED;
            case "FAILED" -> OrderStatus.CANCELED;
            default -> OrderStatus.PENDING;
        };
    }
}
