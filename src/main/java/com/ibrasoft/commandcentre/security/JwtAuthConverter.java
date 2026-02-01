package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.model.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public final class JwtAuthConverter {

    private JwtAuthConverter() {
    }

    public static List<SimpleGrantedAuthority> toAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
        }
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }
}
