package br.com.service.pament.paymentservice.core.service;

import br.com.service.pament.paymentservice.config.exception.ValidationException;
import br.com.service.pament.paymentservice.core.dto.Event;
import br.com.service.pament.paymentservice.core.dto.OrderProducts;
import br.com.service.pament.paymentservice.core.model.Payment;
import br.com.service.pament.paymentservice.core.producer.KafkaProducer;
import br.com.service.pament.paymentservice.core.repository.PaymentRepository;
import br.com.service.pament.paymentservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    public static final Double REDUCE_SUM_VALUE = 0.0;
    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) throws JsonProcessingException {

        try {
            checkCurrentValidation(event);
            createPendingPayment(event);
        }catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));

    }

    private void checkCurrentValidation(Event event) {

        // validando indepotencia
        if(paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There's another transactionId for this validation!");
        }

    }

    private void createPendingPayment(Event event) {

        var totalAmount = calculateAmout(event);
        var totalItems = calculateTotalItems(event);

        Payment payment = Payment.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        setEventAmoutItems(event, payment);

    }

    private double calculateAmout(Event event){
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

    private void setEventAmoutItems(Event event, Payment payment){
        event.getPayload().setTotalAmount(payment.getTotalAmount());
        event.getPayload().setTotalItems(payment.getTotalItems());
    }

    private void save(Payment payment){
        paymentRepository.save(payment);
    }

}
