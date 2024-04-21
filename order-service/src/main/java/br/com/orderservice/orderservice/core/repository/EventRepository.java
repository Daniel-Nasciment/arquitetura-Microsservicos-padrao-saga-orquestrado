package br.com.orderservice.orderservice.core.repository;

import br.com.orderservice.orderservice.core.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
}
