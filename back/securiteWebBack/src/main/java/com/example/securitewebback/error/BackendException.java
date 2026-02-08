package com.example.securitewebback.error;

import org.springframework.http.HttpStatusCode;
import lombok.Getter;

@Getter
public class BackendException extends RuntimeException {
    private final HttpStatusCode status;
    private final String responseBody;

    public BackendException(HttpStatusCode status, String responseBody) {
        super("Erreur provenant du Backend B: " + status);
        this.status = status;
        this.responseBody = responseBody;
    }
}
