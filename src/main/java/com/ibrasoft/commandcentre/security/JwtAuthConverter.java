package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.model.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtAuthConverter {

    private static final Set<String> KNOWN_ROLES = Arrays.stream(Role.values())
        .map(Role::name)
        .collect(Collectors.toUnmodifiableSet());

    private JwtAuthConverter() {
    }

    /**
     * Maps the token's {@code roles} claim to authorities. Only the role names defined in
     * {@link Role} are honoured; unknown values are dropped. A missing or empty claim yields
     * no authorities at all - it never falls back to a default role.
     */
    public static List<SimpleGrantedAuthority> toAuthorities(List<?> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .filter(KNOWN_ROLES::contains)
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
}
