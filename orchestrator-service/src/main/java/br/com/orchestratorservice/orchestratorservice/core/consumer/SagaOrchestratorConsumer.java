package br.com.orchestratorservice.orchestratorservice.core.consumer;

import br.com.orchestratorservice.orchestratorservice.core.dto.Event;
import br.com.orchestratorservice.orchestratorservice.core.utils.JsonUtil;
import br.com.orchestratorservice.orchestratorservice.service.OrchestratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class SagaOrchestratorConsumer {

    private final JsonUtil jsonUtil;
    private final OrchestratorService orchestratorService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.start-saga}"}
    )
    public void consumeStartSagaEvent(String payload) throws JsonProcessingException {
        log.info("Receiving event {} from start-saga topic", payload);
        orchestratorService.startSaga(jsonUtil.toEvent(payload));
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.orchestrator}"}
    )
    public void consumeOrchestratorEvent(String payload) throws JsonProcessingException {
        log.info("Receiving event {} from orchestrator topic", payload);
        orchestratorService.continueSaga(jsonUtil.toEvent(payload));
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.finish-success}"}
    )
    public void consumeFinishSuccessEvent(String payload) throws JsonProcessingException {
        log.info("Receiving event {} from finish-success topic", payload);
       orchestratorService.finishSagaSuccess(jsonUtil.toEvent(payload));
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${spring.kafka.topic.finish-fail}"}
    )
    public void consumeFinishFailEvent(String payload) throws JsonProcessingException {
        log.info("Receiving event {} from finish-fail topic", payload);
        orchestratorService.finishSagaFail(jsonUtil.toEvent(payload));
    }
}
