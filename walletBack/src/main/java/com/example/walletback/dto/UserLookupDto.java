
package com.example.walletback.dto; 

import java.util.UUID;

public record UserLookupDto(UUID ssoId, String email) {
}
