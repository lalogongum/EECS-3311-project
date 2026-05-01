package com.consulthub.repository;
import com.consulthub.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    List<PaymentEntity> findByClientId(String clientId);
}
