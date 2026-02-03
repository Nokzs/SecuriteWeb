
import java.math.BigDecimal;

public record userDto(
    String email,      // Majuscule ici
    String role,       // Et ici
    BigDecimal balance
) {
    public static userDto fromEntity(User user) { // Ajout de 'static'
        return new userDto(
            user.getEmail(),
            user.getRole().name(),
            user.getBalance()
        );
    }
}
