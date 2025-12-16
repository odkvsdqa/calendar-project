package com.calendar.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class VerificationService {

	private static final Logger log = LoggerFactory.getLogger(VerificationService.class);

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private EmailService emailService;

	// 設定：驗證碼 5 分鐘有效
	private static final long CODE_EXPIRE_MINUTES = 5;
	// 設定：單一 IP 每天最多發送 10 次
	private static final int MAX_SEND_PER_DAY = 10;

	/**
	 * 發送驗證碼 (含 IP 限流與 Redis 儲存)
	 */
	public void sendCode(String email, String purpose, HttpServletRequest request) throws Exception {
		String ip = getClientIp(request);
		log.info("收到發送驗證碼請求 | Email: {} | Purpose: {} | IP: {}", email, purpose, ip);

		// 1. IP 限流檢查
		if (isRateLimited(ip)) {
			log.warn("IP {} 發送頻率過高", ip);
			throw new RuntimeException("發送過於頻繁，請稍後再試");
		}

		// 2. 生成 6 位數驗證碼
		String code = String.format("%06d", new Random().nextInt(999999));

		// 3. 存入 Redis (Key: "verify:purpose:email")
		String redisKey = "verify:" + purpose + ":" + email;
		redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

		// 4. 發送郵件
		emailService.sendVerificationCode(email, purpose, code);

		log.info("驗證碼已發送至 {}", email);
	}

	/**
	 * 驗證代碼
	 */
	public boolean verify(String email, String purpose, String inputCode) {
		String redisKey = "verify:" + purpose + ":" + email;
		String storedCode = redisTemplate.opsForValue().get(redisKey);

		if (storedCode != null && storedCode.equals(inputCode)) {
			// 驗證成功後刪除，防止重複使用
			redisTemplate.delete(redisKey);
			return true;
		}

		return false;
	}

	/**
	 * 檢查 IP 限流
	 */
	private boolean isRateLimited(String ip) {
		String key = "rate_limit:ip:" + ip;
		Long count = redisTemplate.opsForValue().increment(key);

		// 如果是第一次訪問，設定 24 小時過期
		if (count != null && count == 1) {
			redisTemplate.expire(key, 24, TimeUnit.HOURS);
		}

		return count != null && count > MAX_SEND_PER_DAY;
	}

	/**
	 * 獲取真實 IP
	 */
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}