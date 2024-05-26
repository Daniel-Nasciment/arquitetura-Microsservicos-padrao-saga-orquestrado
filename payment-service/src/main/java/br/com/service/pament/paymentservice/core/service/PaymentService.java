package br.com.service.pament.paymentservice.core.service;

import br.com.service.pament.paymentservice.config.exception.ValidationException;
import br.com.service.pament.paymentservice.core.dto.Event;
import br.com.service.pament.paymentservice.core.dto.History;
import br.com.service.pament.paymentservice.core.dto.OrderProducts;
import br.com.service.pament.paymentservice.core.enums.EPaymentStatus;
import br.com.service.pament.paymentservice.core.enums.ESagaStatus;
import br.com.service.pament.paymentservice.core.model.Payment;
import br.com.service.pament.paymentservice.core.producer.KafkaProducer;
import br.com.service.pament.paymentservice.core.repository.PaymentRepository;
import br.com.service.pament.paymentservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.service.pament.paymentservice.core.enums.EPaymentStatus.REFOUND;
import static br.com.service.pament.paymentservice.core.enums.EPaymentStatus.SUCCESS;
import static br.com.service.pament.paymentservice.core.enums.ESagaStatus.FAIL;
import static br.com.service.pament.paymentservice.core.enums.ESagaStatus.ROLLBACK_PENDING;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    public static final Double REDUCE_SUM_VALUE = 0.0;
    public static final Double MIN_AMOUNT_VALUE = 0.1;
    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) throws JsonProcessingException {

        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
            Payment payment = findByOrderIdAndTransactionId(event);
            validateAmount(payment.getTotalAmount());
            changePaymentToSuccess(payment);
            handleSuccess(event);
        }catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event), "");

    }



    private void checkCurrentValidation(Event event) {

        // validando indepotencia
        if(paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There's another transactionId for this validation!");
        }

    }

    private void createPendingPayment(Event event) {

        var totalAmount = calculateAmount(event);
        var totalItems = calculateTotalItems(event);

        Payment payment = Payment.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        setEventAmountItems(event, payment);
        save(payment);

    }

    private double calculateAmount(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
                .reduce(REDUCE_SUM_VALUE, Double::sum);
    }

    private int calculateTotalItems(Event event){
        return event
                .getPayload()
                .getProducts()
                .stream()
                .map(OrderProducts::getQuantity)
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
    }

    private void setEventAmountItems(Event event, Payment payment){
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }


    private void validateAmount(double amount){
        if(amount < MIN_AMOUNT_VALUE) {
            throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_VALUE.toString()));
        }
    }

    private Payment findByOrderIdAndTransactionId(Event event){
        return paymentRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId()).orElseThrow(() -> new ValidationException("Payment not found by OrderId and TransactionId"));
    }

    private void changePaymentToSuccess(Payment payment) {
        payment.setStatus(SUCCESS);
        save(payment);
    }

    private void save(Payment payment){
        paymentRepository.save(payment);
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Payment realized successfully!");
    }

    private void addHistory(Event event, String message){
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdBy(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }


    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to realize payment: ".concat(message));
    }


    public void realizeRefound(Event event) throws JsonProcessingException {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);

        try {
            changePaymentStatusToRefound(event);
            addHistory(event, "Rollback executed for payment!");
        }catch (Exception ex) {
            log.error( "Rollback not executed for payment: ", ex);
            addHistory(event, "Rollback not executed for payment: ".concat(ex.getMessage()));
        }


        kafkaProducer.sendEvent(jsonUtil.toJson(event), "");
    }

    private void changePaymentStatusToRefound(Event event) {
        Payment payment = findByOrderIdAndTransactionId(event);
        payment.setStatus(REFOUND);
        setEventAmountItems(event, payment);
        save(payment);
    }

}
