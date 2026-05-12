package br.edu.utfpr.tac.cognito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthResDTO {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
}
