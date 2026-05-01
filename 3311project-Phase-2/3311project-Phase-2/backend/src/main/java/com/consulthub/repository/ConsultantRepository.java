package com.consulthub.repository;
import com.consulthub.entity.ConsultantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConsultantRepository extends JpaRepository<ConsultantEntity, String> {}
