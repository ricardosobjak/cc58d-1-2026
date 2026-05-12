package br.edu.utfpr.apicore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "User resource endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("{id}")
    public User get(@PathVariable Long id) {
        return userService.findById(id);
    }
    // public ResponseEntity<Object> get(@PathVariable Long id) {
    //     var user = userService.findById(id);

    //     return user.isPresent()
    //         ? ResponseEntity.ok(user.get())
    //         : ResponseEntity.notFound().build();
    // }


    @Operation(summary = "Retrive all users", description = "Get all users")
    @ApiResponses({
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "500")
    })
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Object> getAll() {
        return ResponseEntity
            .status(200)
            .body(userService.getAll());
    }

    @GetMapping("name/{name}")
    public ResponseEntity<Object> getByName(
        @PathVariable String name, Pageable pageable) {

        return ResponseEntity
            .status(206)
            .body(userService.getByName(name, pageable));
    }

    @PostMapping
    public User create(@RequestBody @Valid UserDTO dto) {
        System.out.println("Criando usuário: " + dto);
        return userService.create(dto);
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, 
        @Valid @RequestBody UserDTO dto) {
        System.out.println("Atualizando usuário: " + dto);
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        System.out.println("Deletando usuário: " + id);
        userService.delete(id);
    }

}
