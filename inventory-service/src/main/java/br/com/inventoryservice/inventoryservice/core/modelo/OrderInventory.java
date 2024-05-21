package br.com.inventoryservice.inventoryservice.core.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "order_inventory")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;
    @Column(nullable = false)
    private String orderId;
    @Column(nullable = false)
    private String transactionId;
    @Column(nullable = false)
    private Integer orderQuantity;
    @Column(nullable = false)
    private Integer oldQuantity;
    @Column(nullable = false)
    private Integer newQuantity;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
