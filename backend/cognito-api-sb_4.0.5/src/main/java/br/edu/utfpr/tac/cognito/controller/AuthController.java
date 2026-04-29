package br.edu.utfpr.tac.cognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.tac.cognito.dto.AuthReqDTO;
import br.edu.utfpr.tac.cognito.dto.AuthResDTO;
import br.edu.utfpr.tac.cognito.dto.RefreshTokenReqDTO;
import br.edu.utfpr.tac.cognito.dto.RefreshTokenResDTO;
import br.edu.utfpr.tac.cognito.dto.TokenDecodeResDTO;
import br.edu.utfpr.tac.cognito.dto.TokenValidationResDTO;
import br.edu.utfpr.tac.cognito.service.AuthService;
import jakarta.validation.Valid;

/**
 * Controlador de autenticação
 * 
 * Responsável por expor os endpoints de login, validação de token, refresh de token e decodificação de token.
 * Cada endpoint recebe as requisições, delega a lógica de negócio para o AuthService e retorna as respostas apropriadas.
 * 
 * Endpoints:
 * - POST /auth/login: Recebe as credenciais do usuário (email e senha) e retorna os tokens de acesso, ID e refresh.
 * - POST /auth/validate: Recebe um token de acesso no header Authorization e retorna se o token é válido ou não.
 * - POST /auth/refresh: Recebe um refresh token e retorna um novo access token e ID token.
 * - POST /auth/decode: Recebe um token de acesso no header Authorization e retorna as informações decodificadas do token.
 * 
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * Endpoint de login, recebe email e senha, delega a autenticação para o AuthService e retorna os tokens de acesso, ID e refresh.
     * 
     * @param authRequest
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResDTO> login(@RequestBody AuthReqDTO authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    /**
     * Endpoint de validação de token, recebe um token de acesso no header Authorization, delega a validação para o AuthService e retorna se o token é válido ou não.
     * 
     * @param authorizationHeader
     * @return
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResDTO> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(authService.validateToken(authorizationHeader));
    }

    /**
     * Endpoint de refresh de token, recebe um refresh token e retorna um novo access token e ID token.
     * 
     * @param request
     * @return
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResDTO> refreshToken(@RequestBody @Valid RefreshTokenReqDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Endpoint de decodificação de token, recebe um token de acesso no header Authorization e retorna as informações decodificadas do token.
     * 
     * @param authorizationHeader
     * @return
     */
    @PostMapping("/decode")
    public ResponseEntity<TokenDecodeResDTO> decode(
            @RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(authService.decodeIdToken(authorizationHeader));
    }
}
