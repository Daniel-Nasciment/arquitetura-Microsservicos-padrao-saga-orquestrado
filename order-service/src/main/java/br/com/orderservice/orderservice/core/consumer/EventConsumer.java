package br.com.orderservice.orderservice.core.consumer;

import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.service.EventService;
import br.com.orderservice.orderservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class EventConsumer {

    private final JsonUtil jsonUtil;
    private final EventService eventService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.notify-ending}"}
    )
    public void consumeNotifyEndingEvent(String payload) throws JsonProcessingException {
        log.info("Receiving ending notfication event {} from notify-ending topic", payload);
        Event event = jsonUtil.toEvent(payload);
        eventService.notifyEnding(event);
    }
}
