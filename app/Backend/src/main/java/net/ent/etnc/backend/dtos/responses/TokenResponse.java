package net.ent.etnc.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ent.etnc.backend.dtos.UserDto;

/**
 * Réponse d'authentification contenant l'access token JWT.
 *
 * <p>Le refresh token n'est pas inclus ici — il est transmis exclusivement
 * via un cookie HttpOnly pour ne pas être lisible en JavaScript.
 */
@Getter
@Setter
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDto userDto;

    public TokenResponse(String accessToken, Long expiresIn, UserDto userDto) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userDto = userDto;
    }
}
