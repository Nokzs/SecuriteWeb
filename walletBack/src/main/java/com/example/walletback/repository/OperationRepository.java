package com.example.walletback.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.walletback.entities.Operation;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    Page<Operation> findByOrigin_SsoId(UUID ssoId, Pageable pageable);
}
