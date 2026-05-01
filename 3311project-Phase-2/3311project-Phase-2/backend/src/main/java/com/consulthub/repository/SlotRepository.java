package com.consulthub.repository;
import com.consulthub.entity.AvailabilitySlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SlotRepository extends JpaRepository<AvailabilitySlotEntity, String> {
    List<AvailabilitySlotEntity> findByConsultant_ConsultantId(String consultantId);
}
