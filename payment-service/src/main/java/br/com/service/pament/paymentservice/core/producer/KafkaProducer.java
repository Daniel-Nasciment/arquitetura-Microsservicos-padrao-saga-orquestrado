package br.com.service.pament.paymentservice.core.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(String payload, String topic){
        log.info("Sending event to topic {} with data {}", topic, payload);
        kafkaTemplate.send(topic, payload);
    }


}
