package br.edu.utfpr.tac.cognito.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RefreshTokenResDTO {
    private String accessToken;
    private String idToken;
}
