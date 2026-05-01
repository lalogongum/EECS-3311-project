package com.consulthub.repository;
import com.consulthub.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceRepository extends JpaRepository<ServiceEntity, String> {}
