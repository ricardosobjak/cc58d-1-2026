package br.edu.utfpr.tac.cognito.dto;

import java.util.Map;

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
public class TokenValidationResDTO {
    private boolean valid;
    private Map<String, Object> claims;
}