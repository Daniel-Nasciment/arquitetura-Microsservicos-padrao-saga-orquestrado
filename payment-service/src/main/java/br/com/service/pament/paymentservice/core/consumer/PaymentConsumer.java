package br.com.service.pament.paymentservice.core.consumer;

import br.com.service.pament.paymentservice.core.dto.Event;
import br.com.service.pament.paymentservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class PaymentConsumer {

    private final JsonUtil jsonUtil;


    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.payment-success}"}
    )
    public void consumeSuccessEvent(String payload) throws JsonProcessingException {
        log.info("Receiving success event {} from payment-success topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.payment-fail}"}
    )
    public void consumeFailEvent(String payload) throws JsonProcessingException {
        log.info("Receiving rollback event {} from payment-fail topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }
}
