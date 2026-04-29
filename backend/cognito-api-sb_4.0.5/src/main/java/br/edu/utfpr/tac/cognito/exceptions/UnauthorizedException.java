package br.edu.utfpr.tac.cognito.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}