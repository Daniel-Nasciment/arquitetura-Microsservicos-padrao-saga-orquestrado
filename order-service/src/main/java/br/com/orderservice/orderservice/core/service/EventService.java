package br.com.orderservice.orderservice.core.service;

import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event save (Event event){
        return eventRepository.save(event);
    }
}
