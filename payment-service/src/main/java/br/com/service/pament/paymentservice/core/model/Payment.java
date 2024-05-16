package br.com.service.pament.paymentservice.core.model;

import br.com.service.pament.paymentservice.core.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static br.com.service.pament.paymentservice.core.enums.EPaymentStatus.PENDING;

@Entity
@Data
@Table(name = "payment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String orderId;
    @Column(nullable = false)
    private String transactionId;
    @Column(nullable = false)
    private int totalItems;
    @Column(nullable = false)
    private double totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPaymentStatus status;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.status = PENDING;
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
