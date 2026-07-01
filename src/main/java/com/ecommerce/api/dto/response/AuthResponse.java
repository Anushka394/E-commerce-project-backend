package com.ecommerce.api.dto.response;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String role;

    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
