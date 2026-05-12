package br.edu.utfpr.apicore.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.utfpr.apicore.dto.Msg;

@RestControllerAdvice(annotations = RestController.class, basePackages = "br.edu.utfpr.apicore.controller")
public class ValidationExceptionHandler {

    /**
     * Método para gerar a resposta para erros de validação dos campos do objeto.
     * 
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class})
    public Msg handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new Msg("Invalid fields", errors, ex.getAllErrors());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Msg notFoundExpection(NotFoundException ex) {
        return new Msg(ex.getMessage());
    }

}
