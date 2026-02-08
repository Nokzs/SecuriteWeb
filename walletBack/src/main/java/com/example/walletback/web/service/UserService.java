package com.example.walletback.web.service;

import java.math.BigDecimal;
import java.util.UUID;

import javax.naming.InsufficientResourcesException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.walletback.dto.AddMoneyRequest;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.walletback.dto.TransfertMoneyRequest;
import com.example.walletback.entities.Operation.OperationSign;
import com.example.walletback.repository.UserRepository;
import com.example.walletback.entities.User;
import com.example.walletback.error.InsufficientSoldException;
import com.example.walletback.dto.UserDto;

import com.example.walletback.dto.UserLookupDto;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatusCode;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final OperationService operationService;

    private final WebClient webClient;

    public UserService(UserRepository userRepository, OperationService operationService, WebClient webClient) {
        this.webClient = webClient;
        this.userRepository = userRepository;
        this.operationService = operationService;
    }

    @Transactional
    public boolean addMoney(UUID ssoId, AddMoneyRequest request) {
        return userRepository.findBySsoId(ssoId)
                .map(user -> {
                    BigDecimal amountToAdd = request.getAmount();

                    BigDecimal currentBalance = (user.getBalance() != null) ? user.getBalance() : BigDecimal.ZERO;

                    user.setBalance(currentBalance.add(amountToAdd));

                    operationService.saveOperation(
                            "Dépôt d'argent via Gateway",
                            null,
                            user,
                            amountToAdd,
                            OperationSign.plus);

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void transferMoney(UUID senderSsoId, TransfertMoneyRequest request, String token) {
        User sender = userRepository.findBySsoId(senderSsoId)
                .orElseThrow(() -> new EntityNotFoundException("Expéditeur introuvable"));

        User receiver = userRepository.findByEmail(request.getRecipientEmail())
                .orElseGet(() -> fetchAndCreateFromAppA(request.getRecipientEmail(), token));

        BigDecimal amount = new BigDecimal(request.getAmount());

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientSoldException("Solde insuffisant");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        operationService.saveOperation(
                request.getLabel(),
                receiver,
                sender,
                amount,
                OperationSign.minus);

        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private User fetchAndCreateFromAppA(String email, String token) {
        UserLookupDto ssoUser = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/user/lookup")
                        .queryParam("email", email)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(UserLookupDto.class)
                .block();

        if (ssoUser == null) {
            throw new EntityNotFoundException("L'utilisateur avec l'email " + email + " n'existe pas dans le SSO.");
        }

        User newUser = new User();
        newUser.setEmail(ssoUser.email());
        newUser.setRole("SYNDIC");
        newUser.setSsoId(ssoUser.ssoId());
        newUser.setBalance(BigDecimal.ZERO);

        return userRepository.save(newUser);
    }
}
