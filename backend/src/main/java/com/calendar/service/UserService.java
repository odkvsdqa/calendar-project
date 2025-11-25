package com.calendar.service;

import com.calendar.model.Role;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Spring Security 使用此方法載入用戶(含角色權限)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用戶不存在: " + username));

        // 將用戶角色轉換為 Spring Security 的權限
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * 註冊新用戶
     */
    @Transactional
    public User registerUser(String username, String email, String password) {
        return registerUser(username, email, password, Role.USER);
    }

    /**
     * 註冊新用戶(指定角色)
     */
    @Transactional
    public User registerUser(String username, String email, String password, Role role) {
        // 檢查用戶名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用戶名已存在");
        }

        // 檢查電子郵件是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("電子郵件已被註冊");
        }

        // 創建新用戶
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * 根據用戶名查找用戶
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根據 ID 查找用戶
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * 獲取所有用戶
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 驗證用戶密碼
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}