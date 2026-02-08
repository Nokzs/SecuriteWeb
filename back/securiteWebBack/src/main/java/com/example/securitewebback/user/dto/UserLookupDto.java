package com.example.securitewebback.user.dto; 

import java.util.UUID;

public record UserLookupDto(UUID ssoId, String email) {
}
