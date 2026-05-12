package br.edu.utfpr.apicore.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

// Objeto de transferência de dados
// Dados imutáveis
public record UserDTO(
    @NotBlank
    @Size(min = 2, max = 100)
    String name,

    @NotBlank
    @Size(min = 5, max = 100)
    @Email
    String email,

    @NotBlank
    @Size(min = 3, max = 100)
    String password,

    @PastOrPresent
    LocalDate birth) {
}
