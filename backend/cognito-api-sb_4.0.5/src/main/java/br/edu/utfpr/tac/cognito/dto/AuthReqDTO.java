package br.edu.utfpr.tac.cognito.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthReqDTO {
    private String username;
    private String password;
}
