package br.com.service.product.productservice.core.service;

import br.com.service.product.productservice.config.exception.ValidationException;
import br.com.service.product.productservice.core.dto.Event;
import br.com.service.product.productservice.core.dto.History;
import br.com.service.product.productservice.core.dto.OrderProducts;
import br.com.service.product.productservice.core.enums.ESagaStatus;
import br.com.service.product.productservice.core.model.Validation;
import br.com.service.product.productservice.core.producer.KafkaProducer;
import br.com.service.product.productservice.core.repository.ProductRepository;
import br.com.service.product.productservice.core.repository.ValidationRepository;
import br.com.service.product.productservice.core.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.service.product.productservice.core.enums.ESagaStatus.SUCCESS;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@Slf4j
@AllArgsConstructor
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final KafkaProducer kafkaProducer;
    private final JsonUtil jsonUtil;
    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;


    public void validateExistingProducts(Event event) throws JsonProcessingException {

        try {

            checkCurrentValidation(event);
            createValidation(event, true);
            handleSuccess(event);

        }catch (Exception ex){
            log.error("Error trying to validate products: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(Event event) {
        validateProductsInformed(event);

        // validando indepotencia
        if(validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There's another transactionId for this validation!");
        }

        event.getPayload().getProducts().forEach(product -> {
            validateProductInformed(product);
            validateExistingProduct(product.getProduct().getCode());
        });

    }

    private void validateProductsInformed(Event event){
        if(isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())){
            throw new ValidationException("Products list is empty!");
        }
        if(isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())){
            throw new ValidationException("OrderId and TransactionId must be informed!");
        }
    }

    private void validateProductInformed(OrderProducts product) {
        if(isEmpty(product.getProduct()) || isEmpty(product.getProduct().getCode())){
            throw new ValidationException("Product must be informed!");
        }
    }

    private void validateExistingProduct(String code) {
        if(!productRepository.existsByCode(code)){
            throw new ValidationException("Product does not exists in database!");
        }
    }


    private void createValidation(Event event, boolean success) {
        Validation validation = Validation.builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Products are validated successfully!");
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
