package com.community.library.dto;

import com.community.library.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String realName;
    private User.Role role;
    
    public JwtResponse(String token, Long id, String username, String realName, User.Role role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }
}
