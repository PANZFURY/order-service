package practical.task.common.event;

import lombok.Data;

@Data
public class PaymentCreatedEvent {
    private String paymentId;
    private String orderId;
    private String status;
}
