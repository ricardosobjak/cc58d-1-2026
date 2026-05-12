package br.edu.utfpr.tac.cognito.security;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.edu.utfpr.tac.cognito.exceptions.UnauthorizedException;
import br.edu.utfpr.tac.cognito.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CognitoTokenValidationInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(CognitoTokenValidationInterceptor.class);

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obtém o token do header Authorization
        String authorizationHeader = request.getHeader("Authorization");

        // Verifica se o token está presente e no formato correto
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token não informado");
        }

        // Remove o prefixo "Bearer " para obter o token puro
        String token = authorizationHeader.substring(7);

        try {
            // Decodifica o token para obter o header e extrair o keyId
            DecodedJWT jwt = JWT.decode(token);
            String keyId = jwt.getHeaderClaim("kid").asString();

            // Obtém a chave pública correspondente ao keyId
            PublicKey publicKey = authService.getPublicKey(keyId);

            // Verifica a assinatura do token usando a chave pública
            if (publicKey == null) {
                throw new UnauthorizedException("Chave pública não encontrada");
            }

            // Verifica a assinatura do token usando a chave pública
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
            algorithm.verify(jwt);

            // Seta os claims do token como atributo na requisição para uso posterior
            request.setAttribute("cognitoUser", jwt.getClaims());

            logger.info("Token válido para o usuário {} com claims: {}", jwt.getSubject(), jwt.getClaims());

            // Token válido, permite a continuação da requisição
            return true;
        } catch (JWTVerificationException e) { // Exceção lançada quando a verificação do token falha
            logger.error("Token inválido", e);
            throw new UnauthorizedException("Token inválido");

        } catch (Exception e) { // Exceção genérica para outros erros que possam ocorrer durante a validação
            logger.error("Erro ao validar token", e);
            throw new UnauthorizedException("Erro ao validar token");
        }

    }
}