package com.teste.testejwt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Data @NoArgsConstructor
@Entity
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<AppUser> users;

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
