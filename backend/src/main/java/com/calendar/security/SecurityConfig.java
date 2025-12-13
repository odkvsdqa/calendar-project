package com.calendar.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 🔥 記得開這個
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.calendar.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 🔥 開啟 @PreAuthorize 註解支援 (你的 AdminController 有用到)
public class SecurityConfig {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	@Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler; // 🔥 注入剛寫好的 EntryPoint

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider)
			throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> {
		}) // 使用 WebConfig 的設定

				// 🔥 設定例外處理 (當 401 發生時，誰來處理？)
				.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/api/preferences/timezones").permitAll()
						// 🔥 新增：Swagger 相關路徑全部放行
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
						// 🔥 這裡的規則如果 Controller 上有 @PreAuthorize 可以不用寫，但寫了雙重保險
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						.requestMatchers("/api/venues/**").authenticated()

						.requestMatchers("/api/categories/**").authenticated()

						.requestMatchers("/api/preferences/**").authenticated()

						.requestMatchers("/api/events/**").authenticated()

						.anyRequest().authenticated())
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}