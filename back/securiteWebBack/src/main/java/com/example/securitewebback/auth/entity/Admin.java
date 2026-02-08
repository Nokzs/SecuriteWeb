package com.example.securitewebback.auth.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
public class Admin extends User {

    public Admin() {
        super();
    }
}

