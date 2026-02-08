package com.example.walletback.web.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.walletback.repository.OperationRepository;
import com.example.walletback.entities.Operation;
import com.example.walletback.entities.Operation.OperationSign;
import com.example.walletback.entities.User;

@Service
public class OperationService {

    @Autowired
    OperationRepository operationRepository;

    public Page<Operation> getOperations(UUID ssoId, Pageable pageable) {
        return this.operationRepository.findAllByUserId(ssoId, pageable);
    }

    public void saveOperation(String label, User receiver, User origin, BigDecimal amount, OperationSign sign) {
        Operation operation = Operation.builder()
                .label(label)
                .receiver(receiver)
                .origin(origin)
                .amount(amount)
                .sign(sign)
                .date(LocalDateTime.now())
                .build();
        operationRepository.save(operation);
    }

}
