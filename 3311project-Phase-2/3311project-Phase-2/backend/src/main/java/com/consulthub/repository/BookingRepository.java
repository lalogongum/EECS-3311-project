package com.consulthub.repository;
import com.consulthub.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    List<BookingEntity> findByClientId(String clientId);
    List<BookingEntity> findByConsultantId(String consultantId);
}
