package br.com.service.product.productservice.core.repository;

import br.com.service.product.productservice.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Boolean existsByCode(String code);
}
