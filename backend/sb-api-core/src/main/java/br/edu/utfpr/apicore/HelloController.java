package br.edu.utfpr.apicore;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/")
public class HelloController {

    @GetMapping
    public String hello() {
        return new String("Tudo funcionando por aqui!");
    }
    
}
