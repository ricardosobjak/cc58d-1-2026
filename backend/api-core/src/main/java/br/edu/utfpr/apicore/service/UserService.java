package br.edu.utfpr.apicore.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.edu.utfpr.apicore.dto.UserDTO;
import br.edu.utfpr.apicore.model.User;
import br.edu.utfpr.apicore.repository.UserRepository;

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
        return userRepository.findById(id).get();
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> getByName(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(name, pageable);
    }

}
