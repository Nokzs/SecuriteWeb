package com.example.securitewebback.auth.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Syndic extends User {
    String nomAgence;

    @Column(nullable = true)
    @Nullable
    String adresse;

    public Syndic() {
        super();
    }

    public Syndic(String email, String password, String nomAgence, String adresse, String telephone) {
        super(email, password, Role.SYNDIC, telephone);
        this.adresse = adresse;
        this.nomAgence = nomAgence;
    }

}
