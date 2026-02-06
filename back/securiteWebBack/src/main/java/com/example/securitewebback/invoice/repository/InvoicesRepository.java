package com.example.securitewebback.invoice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.securitewebback.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoicesRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByExpenseBuildingId(UUID buildingId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.destinataire.id = :ownerId " +
            "ORDER BY CASE WHEN i.statut = 'PENDING' THEN 0 ELSE 1 END ASC, i.createdAt DESC")
    Page<Invoice> findByProprietaire(@Param("ownerId") UUID ownerId, Pageable pageable);
}
