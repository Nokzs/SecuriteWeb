package com.example.securitewebback.user.dto;

import com.example.securitewebback.auth.entity.Syndic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyndicDto extends UserDto {
                                private String nomAgence;
                                private String adresse;

                                public SyndicDto(Syndic s) {
                                                                this.setUuid(s.getId());
                                                                this.setEmail(s.getEmail());
                                                                this.setTelephone(s.getTelephone());
                                                                this.setRole(s.getRole());
                                                                this.nomAgence = s.getNomAgence();
                                                                this.adresse = s.getAdresse();
                                }
}
