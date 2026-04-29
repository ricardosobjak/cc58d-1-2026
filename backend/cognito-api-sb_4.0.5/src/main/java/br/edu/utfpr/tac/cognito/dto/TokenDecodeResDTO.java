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
public class TokenDecodeResDTO {
    private Map<String, Object> header;
    private Map<String, Object> payload;
}
