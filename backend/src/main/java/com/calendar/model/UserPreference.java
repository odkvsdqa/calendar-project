package com.calendar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "language", length = 10)
    private String language = "zh-TW"; // 預設繁體中文
    
    // 🔥 v1.1 新增：主題設定
    @Column(name = "theme", length = 20)
    private String theme = "light"; // 預設淺色模式

    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public UserPreference() {}
    
    public UserPreference(User user, String language) {
        this.user = user;
        this.language = language;
        this.theme = "light";
    }
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === Getters and Setters ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}