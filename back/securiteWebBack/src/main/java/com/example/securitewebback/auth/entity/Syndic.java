package com.example.securitewebback.auth.entity;

public class Syndic extends User {
    String nomAgence;
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
