package br.edu.utfpr.apicore.service;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.edu.utfpr.apicore.dto.UserDTO;
import br.edu.utfpr.apicore.exception.NotFoundException;
import br.edu.utfpr.apicore.model.User;
import br.edu.utfpr.apicore.repository.UserRepository;
import jakarta.validation.Valid;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User create(UserDTO dto) {
        User user = new User(); //Instanciando uma entidade JPA

        // Copia as propriedades do DTO para a entidade JPA
        BeanUtils.copyProperties(dto, user);

        // Persistir a entidade JPA
        return userRepository.save(user);
    }

    public List<User> getAll() {
       return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Not found"));
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> getByName(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public User update(Long id, UserDTO dto) {
        var entity = findById(id);

        // Copia as propriedades do dto para a entidade, igonorando id e password
        BeanUtils.copyProperties(dto, entity, "id,password");

        return userRepository.save(entity);
    }

}
