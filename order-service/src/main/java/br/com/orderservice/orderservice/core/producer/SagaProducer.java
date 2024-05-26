package br.com.orderservice.orderservice.core.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SagaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.product-validation-start}")
    private String productValidationStartTopic;

    public void sendEvent(String payload){
        log.info("Sending event to topic {} with data {}", this.productValidationStartTopic, payload);
        kafkaTemplate.send(productValidationStartTopic, payload);
    }


}
