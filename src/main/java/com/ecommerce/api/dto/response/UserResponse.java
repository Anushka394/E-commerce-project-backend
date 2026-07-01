package com.ecommerce.api.dto.response;

import com.ecommerce.api.model.User;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String address;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.address = user.getAddress();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}
