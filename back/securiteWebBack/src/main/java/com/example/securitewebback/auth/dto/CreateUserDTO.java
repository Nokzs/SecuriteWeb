
package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.User;

public interface CreateUserDTO {
    String email();

    String password();

    String telephone();

    Role role();

    User toEntity();
}
