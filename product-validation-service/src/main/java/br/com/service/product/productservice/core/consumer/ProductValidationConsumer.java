package br.com.service.product.productservice.core.consumer;

import br.com.service.product.productservice.core.dto.Event;
import br.com.service.product.productservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class ProductValidationConsumer {

    private final JsonUtil jsonUtil;


    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.product-validation-success}"}
    )
    public void consumeSuccessEvent(String payload) throws JsonProcessingException {
        log.info("Receiving success event {} from product-validation-success topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.product-validation-fail}"}
    )
    public void consumeFailEvent(String payload) throws JsonProcessingException {
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }
}
