package org.tp.sesam.dtos.api;

public record LoginResponse(String token, MeResponse user) { }
