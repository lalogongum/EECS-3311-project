package com.consulthub.repository;

import com.consulthub.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, String> {
    List<PaymentMethodEntity> findByClientId(String clientId);
}
