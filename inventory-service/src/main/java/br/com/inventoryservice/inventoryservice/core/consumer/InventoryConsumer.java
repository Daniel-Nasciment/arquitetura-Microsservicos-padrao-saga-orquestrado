package br.com.inventoryservice.inventoryservice.core.consumer;


import br.com.inventoryservice.inventoryservice.core.dto.Event;
import br.com.inventoryservice.inventoryservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class InventoryConsumer {

    private final JsonUtil jsonUtil;


    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.inventory-success}"}
    )
    public void consumeSuccessEvent(String payload) throws JsonProcessingException {
        log.info("Receiving success event {} from inventory-success topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.inventory-fail}"}
    )
    public void consumeFailEvent(String payload) throws JsonProcessingException {
        log.info("Receiving rollback event {} from inventory-fail topic", payload);
        Event event = jsonUtil.toEvent(payload);
        log.info(event.toString());
    }
}
