package dev.fkreuzer.shotlog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "season")
@Getter
@Setter
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active;

    public Season(String description) {
        this.description = description;
    }

    protected Season() {
    }
}
