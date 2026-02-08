package com.example.securitewebback.payement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.securitewebback.error.BackendException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatusCode;

import java.util.Map;

@Service
@Slf4j
public class PaymentService {
                                                                private final WebClient webClient;
                                                                @Value("${app.paiement.url}")
                                                                private String paiementAppUrl;

                                                                public PaymentService(WebClient webClient) {
                                                                                                                                this.webClient = webClient;
                                                                }

                                                                public void transfertRequest(String toUserId, double amount, String label, String token) {
                                                                                                                                Map<String, Object> body = Map.of(
                                                                                                                                                                                                                                                                "label",
                                                                                                                                                                                                                                                                label,
                                                                                                                                                                                                                                                                "recipientEmail",
                                                                                                                                                                                                                                                                toUserId,
                                                                                                                                                                                                                                                                "amount",
                                                                                                                                                                                                                                                                amount);
                                                                                                                                log.info("Envoi de la requête de transfert à l'App B vers : {}", toUserId);
                                                                                                                                this.webClient.post().uri(paiementAppUrl + "/api/user/transfer").headers(h -> h.setBearerAuth(token))
                                                                                                                                                                                                                                                                .bodyValue(body)
                                                                                                                                                                                                                                                                .retrieve()
                                                                                                                                                                                                                                                                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                                                                                                                                                                                                                                                                                                                                                                                                .defaultIfEmpty("{\"message\": \"Erreur inconnue du backend B\"}")
                                                                                                                                                                                                                                                                                                                                                                                                .flatMap(errorBody -> Mono.error(new BackendException(response.statusCode(), errorBody))))
                                                                                                                                                                                                                                                                .bodyToMono(Void.class)
                                                                                                                                                                                                                                                                .block();
                                                                }
}
