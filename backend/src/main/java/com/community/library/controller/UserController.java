package com.community.library.controller;

import com.community.library.dto.ApiResponse;
import com.community.library.dto.UserDTO;
import com.community.library.entity.User;
import com.community.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(UserDTO.fromEntity(user.get())));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> updates) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            User updatedUser = userService.updateProfile(
                    user.get().getId(),
                    updates.get("realName"),
                    updates.get("email"),
                    updates.get("phone")
            );
            return ResponseEntity.ok(ApiResponse.success("资料更新成功", UserDTO.fromEntity(updatedUser)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> passwordData) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户未登录"));
        }
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码不能为空"));
        }
        
        Optional<User> user = userService.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            userService.changePassword(user.get().getId(), oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
