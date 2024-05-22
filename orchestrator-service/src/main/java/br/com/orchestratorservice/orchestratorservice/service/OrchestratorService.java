package br.com.orchestratorservice.orchestratorservice.service;

import br.com.orchestratorservice.orchestratorservice.core.consumer.SagaOrchestratorConsumer;
import br.com.orchestratorservice.orchestratorservice.core.dto.Event;
import br.com.orchestratorservice.orchestratorservice.core.dto.History;
import br.com.orchestratorservice.orchestratorservice.core.enums.EEventSource;
import br.com.orchestratorservice.orchestratorservice.core.enums.ETopics;
import br.com.orchestratorservice.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.orchestratorservice.orchestratorservice.core.saga.SagaExecutionController;
import br.com.orchestratorservice.orchestratorservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.orchestratorservice.orchestratorservice.core.enums.EEventSource.ORCHESTRATOR;
import static br.com.orchestratorservice.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.orchestratorservice.orchestratorservice.core.enums.ESagaStatus.SUCCESS;
import static br.com.orchestratorservice.orchestratorservice.core.enums.ETopics.NOTIFY_ENDING;

@AllArgsConstructor
@Service
@Slf4j
public class OrchestratorService {

    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer producer;
    private final SagaOrchestratorConsumer consumer;
    private final SagaExecutionController sec;

    public void startSaga(Event event) throws JsonProcessingException {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        var topic = getTopic(event);
        log.info("SAGA STARTED!");
        addHistory(event, "Saga Started!");
        sendToProducerWithTopic(event, topic);
    }



    public void finishSagaSuccess(Event event) throws JsonProcessingException {
        event.setSource(ORCHESTRATOR);
        event.setStatus(SUCCESS);
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}!", event.getId());
        addHistory(event, "Saga finished successfuly!");
        notifyFinishedSaga(event);
    }

    public void finishSagaFail(Event event) throws JsonProcessingException {
        event.setSource(ORCHESTRATOR);
        event.setStatus(FAIL);
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}!", event.getId());
        addHistory(event, "Saga finished with errors!");
        notifyFinishedSaga(event);
    }

    public void continueSaga(Event event) throws JsonProcessingException {
        var topic = getTopic(event);
        log.info("SAGA CONTINUING FOR EVENT {}", event.getId());
        sendToProducerWithTopic(event, topic);
    }

    private void sendToProducerWithTopic(Event event, ETopics topic) throws JsonProcessingException {
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());
    }

    private ETopics getTopic(Event event) {
        return sec.getNextTopic(event);
    }

    private void addHistory(Event event, String message){
        History history = History.builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdBy(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    private void notifyFinishedSaga(Event event) throws JsonProcessingException {
        sendToProducerWithTopic(event, NOTIFY_ENDING);
    }

}
