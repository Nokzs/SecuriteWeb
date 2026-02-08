package com.example.walletback.web;

import com.example.walletback.dto.OperationDto;
import com.example.walletback.entities.Operation;
import com.example.walletback.web.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt; // Import crucial
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/operation")
public class OperationController {

        @Autowired
        private OperationService operationService;

        @GetMapping
        public Page<OperationDto> getOperation(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int limit) {

                String sub = jwt.getSubject();

                Pageable pageable = PageRequest.of(page, limit, Sort.by("date").descending());

                Page<Operation> operationsPage = operationService.getOperations(UUID.fromString(sub), pageable);

                return operationsPage.map(op -> {
                        String displaySign = op.getReceiver() == null ? "PLUS"
                                        : ((op.getOrigin() != null
                                                        && op.getOrigin().getSsoId().equals(UUID.fromString(sub)))
                                                                        ? "MINUS"
                                                                        : "PLUS");
                        return new OperationDto(
                                        op.getOrigin() != null ? op.getOrigin().getEmail() : "Système/Gateway",
                                        op.getAmount(),
                                        op.getDate(),
                                        displaySign,
                                        op.getLabel(),
                                        op.getReceiver() != null ? op.getReceiver().getEmail() : null);
                });
        }
}
