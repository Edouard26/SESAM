package org.tp.sesam.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDto {
    @NotBlank
    @jakarta.validation.constraints.Email
    private String email;
    @NotBlank
    private String password;
}
