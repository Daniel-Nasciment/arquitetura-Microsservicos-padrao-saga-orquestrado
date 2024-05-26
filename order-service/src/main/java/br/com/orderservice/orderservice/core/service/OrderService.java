package br.com.orderservice.orderservice.core.service;

import br.com.orderservice.orderservice.core.document.Event;
import br.com.orderservice.orderservice.core.document.Order;
import br.com.orderservice.orderservice.core.dto.OrderRequest;
import br.com.orderservice.orderservice.core.producer.SagaProducer;
import br.com.orderservice.orderservice.core.repository.OrderRepository;
import br.com.orderservice.orderservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final OrderRepository orderRepository;
    private final JsonUtil jsonUtil;
    private final SagaProducer producer;
    private final EventService eventService;


    public Order createOrder(OrderRequest request) throws JsonProcessingException {
        Order order = Order.builder()
                .products(request.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(
                        String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID())
                )
                .build();
        Order orderSaved = orderRepository.save(order);
        producer.sendEvent(jsonUtil.toJson(eventService.createEvent(orderSaved)));
        return orderSaved;
    }


}
