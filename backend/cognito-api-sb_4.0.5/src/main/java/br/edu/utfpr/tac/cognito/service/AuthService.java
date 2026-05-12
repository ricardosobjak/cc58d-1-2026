package br.edu.utfpr.tac.cognito.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.utfpr.tac.cognito.dto.AuthReqDTO;
import br.edu.utfpr.tac.cognito.dto.AuthResDTO;
import br.edu.utfpr.tac.cognito.dto.RefreshTokenReqDTO;
import br.edu.utfpr.tac.cognito.dto.RefreshTokenResDTO;
import br.edu.utfpr.tac.cognito.dto.TokenDecodeResDTO;
import br.edu.utfpr.tac.cognito.dto.TokenValidationResDTO;
import br.edu.utfpr.tac.cognito.exceptions.AuthException;
import br.edu.utfpr.tac.cognito.exceptions.UnauthorizedException;

/**
 * Serviço de autenticação que interage diretamente com a AWS Cognito para
 * realizar login, renovação de tokens e validação de tokens JWT. Ele encapsula
 * toda a
 * 
 * lógica de comunicação com o Cognito, incluindo a construção dos payloads de
 * autenticação, o cálculo do secret hash, e a verificação das assinaturas dos
 * tokens JWT usando as chaves públicas do Cognito. Este serviço é o coração da
 * integração com o Cognito e é responsável por garantir que os usuários sejam
 * autenticados corretamente e que os tokens sejam válidos antes de conceder
 * acesso aos recursos protegidos da aplicação.
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${aws.cognito.url}")
    private String cognitoUrl;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    private final Map<String, PublicKey> publicKeyCache = new HashMap<>();

    @Autowired
    private RestClient restClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Realiza o login do usuário no Cognito usando o fluxo USER_PASSWORD_AUTH. Ele
     * constrói o payload de autenticação, incluindo o cálculo do secret hash, e
     * envia a requisição para o endpoint do Cognito. Se a autenticação for
     * bem-sucedida, ele parseia a resposta e retorna um DTO contendo os tokens de
     * acesso, ID e refresh, além do tempo de expiração e tipo do token. Em caso de
     * erro, ele lança uma AuthException personalizada com a mensagem apropriada.
     * Este método é o principal ponto de entrada para o processo de autenticação e
     * é responsável por garantir que os usuários sejam autenticados corretamente
     * antes de acessar os recursos protegidos da aplicação.
     * 
     * @param authRequest
     * @return
     */
    public AuthResDTO login(AuthReqDTO authRequest) {

        try {
            String secretHash = calculateSecretHash(authRequest.getUsername());

            Map<String, Object> payload = Map.of(
                    "AuthFlow", "USER_PASSWORD_AUTH",
                    "ClientId", clientId,
                    "AuthParameters", Map.of(
                            "USERNAME", authRequest.getUsername(),
                            "PASSWORD", authRequest.getPassword(),
                            "SECRET_HASH", secretHash));

            String body = objectMapper.writeValueAsString(payload);

            String response = restClient.post()
                    .uri(cognitoUrl)
                    .header("Content-Type", "application/x-amz-json-1.1")
                    .header("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new AuthException("Erro ao autenticar no Cognito", HttpStatus.UNAUTHORIZED);
                    })
                    .body(String.class);

            // 🔥 Parse da resposta
            JsonNode json = objectMapper.readTree(response);
            JsonNode authResult = json.get("AuthenticationResult");

            var auth = new AuthResDTO();
            auth.setAccessToken(authResult.get("AccessToken").asText());
            auth.setIdToken(authResult.get("IdToken").asText());
            auth.setRefreshToken(
                    authResult.has("RefreshToken") ? authResult.get("RefreshToken").asText() : null);
            auth.setExpiresIn(authResult.get("ExpiresIn").asLong());
            auth.setTokenType(authResult.get("TokenType").asText());

            return auth;

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro no login", e);
            throw new AuthException("Erro interno ao autenticar", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Realiza a renovação do token de acesso usando o fluxo REFRESH_TOKEN_AUTH do
     * Cognito. Ele constrói o payload de autenticação para renovação, incluindo o
     * cálculo do secret
     * hash, e envia a requisição para o endpoint do Cognito. Se a renovação for
     * bem-sucedida, ele parseia a resposta e retorna um DTO contendo os novos
     * tokens de acesso e ID. Em caso de erro, ele lança uma UnauthorizedException
     * personalizada com a mensagem apropriada. Este método é utilizado para manter
     * os usuários autenticados sem que eles precisem fazer login novamente, desde
     * que tenham um refresh token válido.
     * 
     * @param request
     * @return
     */
    public RefreshTokenResDTO refreshToken(RefreshTokenReqDTO request) {
        logger.info("Iniciando processo de refresh token para usuário: {}", request.getUsername());
        logger.debug("Request de refresh token: {}", request);

        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new UnauthorizedException("refresh_token_required");
        }

        try {
            // ATENÇÃO: O 'request.getUsername()' aqui deve ser o username real do Cognito
            // (ex: o UUID/sub),
            // e não o email/alias, caso você permita login por email.
            String secretHash = calculateSecretHash(request.getUsername());

            Map<String, Object> payload = Map.of(
                    "AuthFlow", "REFRESH_TOKEN_AUTH",
                    "ClientId", clientId,
                    "AuthParameters", Map.of(
                            "REFRESH_TOKEN", request.getRefreshToken(),
                            // A chave "USERNAME" foi removida daqui, pois é proibida neste fluxo.
                            "SECRET_HASH", secretHash));

            String body = objectMapper.writeValueAsString(payload);

            String response = restClient.post()
                    .uri(cognitoUrl)
                    .header("Content-Type", "application/x-amz-json-1.1")
                    .header("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        try {
                            String errorBody = new String(res.getBody().readAllBytes());
                            logger.error("Erro Cognito - Status: {}", res.getStatusCode());
                            logger.error("Erro Cognito - Body: {}", errorBody);
                            throw new UnauthorizedException("Erro Cognito: " + errorBody);
                        } catch (Exception e) {
                            throw new UnauthorizedException("Erro ao ler resposta do Cognito");
                        }
                    })
                    .body(String.class);

            JsonNode json = objectMapper.readTree(response);
            JsonNode authResult = json.get("AuthenticationResult");

            if (authResult == null) {
                throw new UnauthorizedException("invalid_refresh_token_response");
            }

            var result = new RefreshTokenResDTO();
            result.setAccessToken(authResult.get("AccessToken").asText());
            result.setIdToken(authResult.get("IdToken").asText());

            return result;

        } catch (UnauthorizedException e) {
            logger.error("Erro ao renovar token", e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao renovar token", e);
            throw new RuntimeException("refresh_token_processing_error");
        }
    }

    /**
     * Valida um token JWT recebido no header Authorization. Ele decodifica o token,
     * extrai o "kid" do header, obtém a chave pública correspondente do Cognito,
     * e verifica a assinatura do token usando essa chave. Se a validação for
     * bem-sucedida, ele retorna um DTO contendo um flag de sucesso e os claims do
     * token.
     * 
     * @param authorizationHeader
     * @return
     */
    public TokenValidationResDTO validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token não informado");
        }

        String token = authorizationHeader.substring(7);

        try {
            DecodedJWT jwt = JWT.decode(token);
            String keyId = jwt.getHeaderClaim("kid").asString();

            PublicKey publicKey = getPublicKey(keyId);

            if (publicKey == null) {
                throw new UnauthorizedException("Chave pública não encontrada");
            }

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
            algorithm.verify(jwt);

            Map<String, Object> claims = jwt.getClaims()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().as(Object.class)));

            return new TokenValidationResDTO(true, claims);

        } catch (JWTVerificationException e) {
            throw new UnauthorizedException("Token inválido");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar token");
        }
    }

    /**
     * Decodifica um token JWT sem validar sua assinatura. Útil para obter
     * informações do token, como o "sub" (user ID) ou outros claims, sem precisar
     * verificar a assinatura. Deve ser usado com cuidado, pois não garante a
     * autenticidade do token.
     * 
     * @param token
     * @return
     */
    public TokenDecodeResDTO decodeIdToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token não informado");
        }

        String token = authorizationHeader.substring(7);

        try {
            String[] chunks = token.split("\\.");

            // No JWT: chunks[0] é Header, chunks[1] é Payload
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]), StandardCharsets.UTF_8);
            String payloadJson = new String(Base64.getUrlDecoder().decode(chunks[1]), StandardCharsets.UTF_8);

            TokenDecodeResDTO response = new TokenDecodeResDTO();

            // Converte os JSONs diretamente para Maps
            response.setHeader(objectMapper.readValue(headerJson, Map.class));
            response.setPayload(objectMapper.readValue(payloadJson, Map.class));

            return response;

        } catch (Exception e) {
            logger.error("Erro ao decodificar partes do token", e);
            throw new RuntimeException("Erro ao processar estrutura do JWT", e);
        }
    }

    /**
     * Obtém a chave pública do Cognito para um dado keyId (kid).
     * O Cognito publica suas chaves públicas em um endpoint específico,
     * e cada token JWT contém um "kid" que indica qual chave foi usada para assinar
     * o token.
     * 
     * Este método busca a chave correspondente ao "kid" fornecido, decodifica os
     * parâmetros "n" e "e" da chave RSA, e constrói um objeto PublicKey
     * que pode ser usado para verificar a assinatura dos tokens JWT.
     * 
     * @param keyId
     * @return
     */
    public PublicKey getPublicKey(String keyId) {

        if (publicKeyCache.containsKey(keyId)) {
            return publicKeyCache.get(keyId);
        }

        try {
            String jwksUrl = String.format("%s/%s/.well-known/jwks.json", cognitoUrl, userPoolId);

            String response = restClient.get()
                    .uri(jwksUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode body = objectMapper.readTree(response);
            JsonNode keys = body.get("keys");

            if (keys != null && keys.isArray()) {
                for (JsonNode key : keys) {

                    if (key.has("kid") && key.get("kid").asText().equals(keyId)) {

                        String nStr = key.get("n").asText();
                        String eStr = key.get("e").asText();

                        byte[] nBytes = Base64.getUrlDecoder().decode(nStr);
                        byte[] eBytes = Base64.getUrlDecoder().decode(eStr);

                        RSAPublicKeySpec spec = new RSAPublicKeySpec(
                                new BigInteger(1, nBytes),
                                new BigInteger(1, eBytes));

                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey publicKey = keyFactory.generatePublic(spec);

                        publicKeyCache.put(keyId, publicKey);
                        return publicKey;
                    }
                }
            }

            logger.error("Chave pública não encontrada para kid: {}", keyId);

        } catch (Exception e) {
            logger.error("Erro ao buscar chaves públicas do Cognito", e);
        }

        return null;
    }

    /**
     * Calcula o secret hash necessário para autenticação no Cognito. O secret hash
     * é uma combinação do username, clientId e clientSecret, e é usado para
     * garantir a integridade das requisições de autenticação.
     * 
     * O cálculo é feito usando HMAC-SHA256, onde a mensagem é a concatenação do
     * username e clientId, e a chave é o clientSecret. O resultado é então
     * codificado em Base64 para ser enviado ao Cognito.
     * 
     * ATENÇÃO: O valor de "input" deve ser o username real do Cognito (ex: o
     * UUID/sub), e não o email/alias, caso permita login por email. O
     * Cognito espera que o secret hash seja calculado com o valor do "USERNAME"
     * que está sendo enviado na requisição de autenticação, e esse valor deve
     * corresponder ao username real do usuário no Cognito, não ao email ou outro
     * alias.
     * 
     * @param input
     * @return
     * @throws Exception
     */
    private String calculateSecretHash(String input) throws Exception {
        String message = input + clientId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

}
