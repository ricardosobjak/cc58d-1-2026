package br.edu.utfpr.apicore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.apicore.dto.UserDTO;
import br.edu.utfpr.apicore.model.User;
import br.edu.utfpr.apicore.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("{id}")
    public User get(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping(value = {"", "/"})
    public List<User> getAll() {
        return userService.getAll();
    }


    @GetMapping("name/{name}")
    public Page<User> getByName(@PathVariable String name, Pageable pageable) {
        return userService.getByName(name, pageable);
    }

    @PostMapping
    public User create(@RequestBody @Valid UserDTO dto) {
        System.out.println("Criando usuário: " + dto);
        return userService.create(dto);
    }

    @PutMapping("{id}")
    public String update(@PathVariable String id, @RequestBody String entity) {
        System.out.println("Atualizando usuário: " + entity);

        return entity;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        System.out.println("Deletando usuário: " + id);
        userService.delete(id);
    }

}
