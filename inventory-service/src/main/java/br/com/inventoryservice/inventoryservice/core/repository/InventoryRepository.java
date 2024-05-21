package br.com.inventoryservice.inventoryservice.core.repository;

import br.com.inventoryservice.inventoryservice.core.modelo.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductCode(String productCode);
}
