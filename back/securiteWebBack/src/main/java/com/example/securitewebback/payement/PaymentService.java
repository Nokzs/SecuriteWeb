package com.example.securitewebback.payement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatusCode;
import java.util.Map;
import java.util.UUID;


@Service
public class PaymentService {
    private final WebClient webClient;
    @Value("${app.paiement.url}")
    private String paiementAppUrl;
    public paymentService(WebClient webClient) {
        this.webClient = webClient;
    }
    
    public void transfertRequest( UUID toUserId, double amount) {
        Map<String, Object> body = Map.of(
                "label": "Remboursement pour annulation de dépense",
                "recipientEmail", toUserId,
                "amount", amount
                );

        this.webClient.post()
            .uri(paiementAppUrl+"/api/user/transfer")
            .bodyValue(body)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> {
                return Mono.error(new RuntimeException("Échec du transfert de fonds vers l'App B"));
            })
        .bodyToMono(Void.class)
            .block(); 
    }
}

