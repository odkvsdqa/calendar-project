package com.calendar.dto;

import com.calendar.model.User;

public class UserDTO {

    private String id;
    private String username;
    private String email;
    private String role;  // 新增角色欄位

    public UserDTO() {}

    public UserDTO(String id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // 從 User 實體轉換為 DTO
    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()  // ⚠️ 確認這行存在
        );
        System.out.println("UserDTO 角色: " + dto.getRole());  // ⚠️ Debug
        return dto;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}