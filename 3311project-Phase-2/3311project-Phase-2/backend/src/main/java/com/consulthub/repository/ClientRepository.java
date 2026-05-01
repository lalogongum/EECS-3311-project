package com.consulthub.repository;
import com.consulthub.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClientRepository extends JpaRepository<ClientEntity, String> {}
