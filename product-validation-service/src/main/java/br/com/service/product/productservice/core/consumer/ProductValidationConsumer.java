package br.com.service.product.productservice.core.consumer;

import br.com.service.product.productservice.core.dto.Event;
import br.com.service.product.productservice.core.service.ProductValidationService;
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
    private final ProductValidationService productValidationService;


    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.product-validation-start}"}
    )
    public void consumeSuccessEvent(String payload) throws JsonProcessingException {
        log.info("Receiving success event {} from product-validation-start topic", payload);
        Event event = jsonUtil.toEvent(payload);
        productValidationService.validateExistingProducts(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.product-validation-fail}"}
    )
    public void consumeFailEvent(String payload) throws JsonProcessingException {
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        Event event = jsonUtil.toEvent(payload);
        productValidationService.rollbackEvent(event);
    }
}
