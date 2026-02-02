package com.ibrasoft.commandcentre.security;

import java.util.List;

public record JwtClaims(String subject, List<String> roles) {}
