
package com.example.walletback.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.example.walletback.entities.Operation;
import com.example.walletback.web.service.OperationService;
import com.example.walletback.dto.OperationDto;

@RestController
@RequestMapping("/api/operation")
public class OperationController {

        @Autowired
        private OperationService operationService;

        @GetMapping
        public Page<OperationDto> getOperation(
                        @AuthenticationPrincipal OidcUser principal,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int limit) {
                String sub = principal.getSubject();

                Pageable pageable = PageRequest.of(page, limit, Sort.by("date").descending());

                Page<Operation> operationsPage = operationService.getOperations(UUID.fromString(sub), pageable);
                return operationsPage.map(op -> new OperationDto(
                                op.getOrigin().getEmail(),
                                op.getAmount(),
                                op.getDate(),
                                op.getSign().name(),
                                op.getLabel(),
                                op.getReceiver() != null ? op.getReceiver().getEmail() : null));
        }

}
