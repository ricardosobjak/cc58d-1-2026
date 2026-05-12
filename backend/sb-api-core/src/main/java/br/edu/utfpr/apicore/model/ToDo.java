package br.edu.utfpr.apicore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

// Afazer

@Entity
@Table(name = "tb_todo")
@Data //Gera getters, setters, construtores, tostring,...
public class ToDo extends BaseEntity {
    private String title;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
