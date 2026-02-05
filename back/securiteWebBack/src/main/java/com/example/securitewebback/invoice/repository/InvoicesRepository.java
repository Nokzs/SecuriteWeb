package com.example.securitewebback.invoice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securitewebback.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoicesRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByExpenseBuildingId(UUID buildingId, Pageable pageable);

    Page<Invoice> findBySyndic(UUID syndicUuid, Pageable pageable);
}
