package br.com.inventoryservice.inventoryservice.core.service;

import br.com.inventoryservice.inventoryservice.config.exception.ValidationException;
import br.com.inventoryservice.inventoryservice.core.dto.Event;
import br.com.inventoryservice.inventoryservice.core.dto.OrderProducts;
import br.com.inventoryservice.inventoryservice.core.dto.Product;
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
        }catch (Exception ex) {
            log.error("Error trying to update inventory: ", ex);
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
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


}
