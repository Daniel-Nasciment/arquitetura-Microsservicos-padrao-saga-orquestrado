package br.com.inventoryservice.inventoryservice.core.repository;

import br.com.inventoryservice.inventoryservice.core.modelo.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Integer> {
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);

}
