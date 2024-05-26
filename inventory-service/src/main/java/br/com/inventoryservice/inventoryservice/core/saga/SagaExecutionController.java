package br.com.inventoryservice.inventoryservice.core.saga;



import br.com.inventoryservice.inventoryservice.core.dto.Event;
import br.com.inventoryservice.inventoryservice.core.producer.KafkaProducer;
import br.com.inventoryservice.inventoryservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
@Slf4j
@RequiredArgsConstructor
public class SagaExecutionController {

    public static final String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID %s | EVENT ID %s";
    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;

    @Value("${spring.kafka.topic.inventory-fail}")
    private String inventoryFailTopic;
    @Value("${spring.kafka.topic.payment-fail}")
    private String paymentFailTopic;
    @Value("${spring.kafka.topic.notify-ending}")
    private String notifyEndingTopic;



    public void handleSaga(Event event) throws JsonProcessingException {
        switch (event.getStatus()){
            case SUCCESS -> handleSuccess(event);
            case ROLLBACK_PENDING -> handleRollbackPending(event);
            case FAIL -> handleFail(event);
        }
    }

    private void handleFail(Event event) throws JsonProcessingException {
        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}", event.getSource(), this.paymentFailTopic, createSagaId(event));
        sendEvent(event, this.paymentFailTopic);
    }

    private void handleRollbackPending(Event event) throws JsonProcessingException {
        log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}", event.getSource(), this.inventoryFailTopic, createSagaId(event));
        sendEvent(event, this.inventoryFailTopic);
    }

    private void handleSuccess(Event event) throws JsonProcessingException {
        log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}", event.getSource(), this.notifyEndingTopic, createSagaId(event));
        sendEvent(event, this.notifyEndingTopic);
    }


    private String createSagaId(Event event){
        return format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
    }

    private void sendEvent(Event event, String topic) throws JsonProcessingException {
        String json = jsonUtil.toJson(event);
        kafkaProducer.sendEvent(json, topic);
    }

}
