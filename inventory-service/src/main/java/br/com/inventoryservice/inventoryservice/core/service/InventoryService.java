package br.com.inventoryservice.inventoryservice.core.service;

import br.com.inventoryservice.inventoryservice.config.exception.ValidationException;
import br.com.inventoryservice.inventoryservice.core.dto.*;
import br.com.inventoryservice.inventoryservice.core.enums.ESagaStatus;
import br.com.inventoryservice.inventoryservice.core.modelo.Inventory;
import br.com.inventoryservice.inventoryservice.core.modelo.OrderInventory;
import br.com.inventoryservice.inventoryservice.core.producer.KafkaProducer;
import br.com.inventoryservice.inventoryservice.core.repository.InventoryRepository;
import br.com.inventoryservice.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.inventoryservice.inventoryservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.inventoryservice.inventoryservice.core.enums.ESagaStatus.FAIL;
import static br.com.inventoryservice.inventoryservice.core.enums.ESagaStatus.ROLLBACK_PENDING;

@AllArgsConstructor
@Service
@Slf4j
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";
    private JsonUtil jsonUtil;
    private KafkaProducer kafkaProducer;
    private InventoryRepository inventoryRepository;
    private OrderInventoryRepository orderInventoryRepository;

    public void updateInventory(Event event) throws JsonProcessingException {
        try {
            checkCurrentValidation(event);
            createOrderInventory(event);
            updateInventory(event.getPayload());
            handleSuccess(event);
        }catch (Exception ex) {
            log.error("Error trying to update inventory: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void updateInventory(Order order) {
        order.getProducts().forEach(product -> {
            var inventory = findInventoryByProductCode(product.getProduct().getCode());
            checkInventory(inventory.getAvailable(), product.getQuantity());
            inventory.setAvailable(inventory.getAvailable() -product.getQuantity());
            inventoryRepository.save(inventory);
        });
    }

    private void checkInventory(Integer available, int orderQuantity) {
        if(orderQuantity > available){
            throw new ValidationException("Product is out of stock!");
        }
    }


    private void createOrderInventory(Event event) {
        event.getPayload().getProducts().forEach(product -> {
            var inventory = findInventoryByProductCode(product.getProduct().getCode());
            var orderInventory = createOrderInventory(event, product, inventory);
            orderInventoryRepository.save(orderInventory);
        });
    }

    private OrderInventory createOrderInventory(Event event, OrderProducts product, Inventory inventory){
       return OrderInventory.builder()
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(product.getQuantity())
                .newQuantity(inventory.getAvailable() - product.getQuantity())
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .build();
    }

    private Inventory findInventoryByProductCode(String productCode){
        return inventoryRepository.findByProductCode(productCode).orElseThrow(() -> new ValidationException("Inventory not found by informed product!"));
    }

    private void checkCurrentValidation(Event event) {

        // validando indepotencia
        if(orderInventoryRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There's another transactionId for this validation!");
        }

    }


    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Inventory updated successfully!");
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

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to update inventory: ".concat(message));
    }

    public void rollbackInventory(Event event) throws JsonProcessingException {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);

        try {
            returnInventoryToPreviousValues(event);
            addHistory(event, "Rollback executed for inventory!");
        }catch (Exception ex) {
            log.error( "Rollback not executed for inventory: ", ex);
            addHistory(event, "Rollback not executed for inventory: ".concat(ex.getMessage()));
        }


        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void returnInventoryToPreviousValues(Event event) {
        orderInventoryRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId()).forEach(
                orderInventory -> {
                    var inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {} from {} to {}", event.getPayload().getId(), orderInventory.getNewQuantity(), inventory.getAvailable());
                }
        );
    }

}
