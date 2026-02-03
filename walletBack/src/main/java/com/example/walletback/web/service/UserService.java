package com.example.walletback.web.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.walletback.dto.AddMoneyRequest;
import com.example.walletback.entities.Operation.OperationSign;
import com.example.walletback.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final OperationService operationService;

    public UserService(UserRepository userRepository, OperationService operationService) {
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
                            user,
                            null,
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

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseGet(() -> fetchAndCreateFromAppA(request.getReceiverEmail(), token));

        BigDecimal amount = request.getAmount();

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Solde insuffisant pour effectuer le transfert");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        operationService.saveOperation(
                "Transfert vers " + receiver.getEmail(),
                sender,
                receiver,
                amount,
                OperationSign.minus);

        operationService.saveOperation(
                "Transfert reçu de " + sender.getEmail(),
                receiver,
                sender,
                amount,
                OperationSign.plus);
        
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private User fetchAndCreateFromAppA(String email, String token) {
        UserDto ssoUser = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/user/lookup")
                        .queryParam("email", email) 
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(UserDto.class)
                .block();

        if (ssoUser == null) {
            throw new EntityNotFoundException("L'utilisateur avec l'email " + email + " n'existe pas dans le SSO.");
        }

        User newUser = new User();
        newUser.setEmail(ssoUser.getEmail());
        newUser.setRole("SYNDIC"); 
        newUser.setSsoId(ssoUser.getSsoId());
        newUser.setBalance(BigDecimal.ZERO);
        
        return userRepository.save(newUser);
    }
}
