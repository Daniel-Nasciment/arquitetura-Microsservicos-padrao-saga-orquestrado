package br.com.orderservice.orderservice.core.service;

import br.com.orderservice.orderservice.config.exceptions.ValidationException;
import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.dto.EventFilters;
import br.com.orderservice.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;


@Service
@AllArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(Event event){
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        eventRepository.save(event);
        log.info("Order {} woth saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
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
}
