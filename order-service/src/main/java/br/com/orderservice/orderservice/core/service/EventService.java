package br.com.orderservice.orderservice.core.service;

import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
