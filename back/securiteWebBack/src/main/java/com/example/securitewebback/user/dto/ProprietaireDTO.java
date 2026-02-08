package com.example.securitewebback.user.dto;

import com.example.securitewebback.auth.entity.Proprietaire;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProprietaireDTO extends UserDto {
                private String nom;
                private String prenom;

                public ProprietaireDTO(Proprietaire p) {
                                this.setUuid(p.getId());
                                this.setEmail(p.getEmail());
                                this.setTelephone(p.getTelephone());
                                this.setRole(p.getRole());
                                this.nom = p.getNom();
                                this.prenom = p.getPrenom();
                                this.setFirstLogin(p.getIsFirstLogin());
                }
}
