package br.com.orderservice.orderservice.core.service;

import br.com.orderservice.orderservice.config.exceptions.ValidationException;
import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.document.History;
import br.com.orderservice.orderservice.core.document.Order;
import br.com.orderservice.orderservice.core.dto.EventFilters;
import br.com.orderservice.orderservice.core.enums.ESagaStatus;
import br.com.orderservice.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.orderservice.orderservice.core.enums.ESagaStatus.SUCCESS;
import static org.springframework.util.ObjectUtils.isEmpty;


@Service
@AllArgsConstructor
@Slf4j
public class EventService {

    private static final String CURRENT_SERVICE = "ORDER_SERVICE";
    private final EventRepository eventRepository;

    public void notifyEnding(Event event){
        event.setSource(CURRENT_SERVICE);
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        setEndingHistory(event);
        save(event);
        log.info("Order {} woth saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    private void setEndingHistory(Event event) {
        if(SUCCESS.equals(event.getStatus())){
            log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}!", event.getId());
            addHistory(event, "Saga finished successfuly!");
        } else {
            log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}!", event.getId());
            addHistory(event, "Saga finished with errors!");
        }
    }

    public Event save (Event event){
        return eventRepository.save(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters){
        validateEmptyFilters(filters);
        if(!(isEmpty(filters.getOrderId()))){
            return findByOrderId(filters.getOrderId());
        }
        return findByTransactionId(filters.getTransactionId());
    }

    private void validateEmptyFilters(EventFilters filters){
        if(isEmpty(filters.getOrderId()) && isEmpty(filters.getTransactionId())){
            throw new ValidationException("OrderId or TransacionId must be informed!");
        }
    }

    private Event findByOrderId(String orderId){
        return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("OrderId not found!"));
    }
    private Event findByTransactionId(String transactionId){
        return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("TransactionId not found!"));
    }

    public Event createEvent(Order order){
        var event = Event.builder()
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .source(CURRENT_SERVICE)
                .status(SUCCESS)
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        addHistory(event, "Saga Started!!");
        return save(event);
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

}
