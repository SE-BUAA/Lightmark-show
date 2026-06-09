package top.ortus.lightmark.backend.dto.module;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRecordDTO {
    private String id;
    private String order_id;
    private String transaction_id;
    private String payment_method;
    private BigDecimal amount;
    private int status;
    private LocalDateTime callback_time;
    private LocalDateTime create_time;

    public PaymentRecordDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCallback_time() {
        return callback_time;
    }

    public void setCallback_time(LocalDateTime callback_time) {
        this.callback_time = callback_time;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }
}