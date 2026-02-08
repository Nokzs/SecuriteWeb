package com.example.securitewebback.auth.entity;

import com.example.securitewebback.building.entity.Building;
import java.util.List;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Syndic extends User {
    String nomAgence;

    @Column(nullable = true)
    @Nullable
    String adresse;

    @OneToMany(mappedBy = "syndic")
    private List<Building> buildings;

    public Syndic() {
        super();
    }

    public Syndic(String email, String password, String nomAgence, String adresse, String telephone) {
        super(email, password, Role.SYNDIC, telephone);
        this.adresse = adresse;
        this.nomAgence = nomAgence;
    }

}
