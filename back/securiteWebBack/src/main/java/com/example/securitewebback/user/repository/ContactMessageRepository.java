package com.example.securitewebback.user.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.auth.entity.Syndic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.user.entity.ContactMessage;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    Page<ContactMessage> findBySyndic(Syndic syndic, Pageable pageable);

    Page<ContactMessage> findBySyndicId(UUID syndicId, Pageable pageable);

    Page<ContactMessage> findBySyndicIdAndReadFalse(UUID syndicId, Pageable pageable);

    Page<ContactMessage> findBySyndicIdAndArchivedTrue(UUID syndicId, Pageable pageable);

    Optional<ContactMessage> findByIdAndSyndicId(UUID id, UUID syndicId);

}
