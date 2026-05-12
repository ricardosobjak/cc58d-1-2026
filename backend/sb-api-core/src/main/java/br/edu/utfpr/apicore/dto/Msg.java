package br.edu.utfpr.apicore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Msg {
    private String message;
    private Object errors;
    private Object detail;

    public Msg(String message) {
        this.message = message;
    }
}
