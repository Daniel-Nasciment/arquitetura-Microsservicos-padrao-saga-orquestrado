package br.com.orderservice.orderservice.core.repository;

import br.com.orderservice.orderservice.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
