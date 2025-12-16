package com.calendar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender mailSender;

	// 從 application.properties 讀取寄件人信箱
	@Value("${spring.mail.username}")
	private String fromEmail;

	/**
	 * 發送驗證碼郵件 (非同步執行)
	 * 
	 * @param toEmail 收件人
	 * @param purpose 用途 (register/reset/change)
	 * @param code    驗證碼
	 */
	@Async // 🔥 關鍵優化：讓這個方法在背景執行，不卡住使用者的介面
	public void sendVerificationCode(String toEmail, String purpose, String code) {
		try {
			log.info("開始發送郵件給: {}", toEmail);

			// 1. 建立 HTML 格式的郵件
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject(getSubject(purpose));

			// 2. 建立漂亮的 HTML 內容
			String htmlContent = String.format(
					"<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; max-width: 500px;\">"
							+ "  <h2 style=\"color: #667eea;\">SKJL Calendar 驗證碼</h2>" + "  <p>您好，</p>"
							+ "  <p>您的 <strong>%s</strong> 驗證碼為：</p>"
							+ "  <h1 style=\"color: #f59e0b; letter-spacing: 5px; background: #f9f9f9; padding: 10px; text-align: center; border-radius: 5px;\">%s</h1>"
							+ "  <p style=\"color: #666; font-size: 12px;\">此驗證碼將在 <strong>5 分鐘</strong> 後失效。</p>"
							+ "  <hr style=\"border: none; border-top: 1px solid #eee;\" />"
							+ "  <p style=\"color: #999; font-size: 12px;\">若非本人操作，請忽略此郵件。</p>" + "</div>",
					getPurposeName(purpose), // 例如 "註冊帳號"
					code);

			helper.setText(htmlContent, true); // true 代表這是 HTML

			// 3. 發送
			mailSender.send(message);
			log.info("郵件發送成功！");

		} catch (MessagingException e) {
			log.error("郵件發送失敗: {}", e.getMessage());
			// 這裡可以選擇是否要拋出異常，或者靜默處理
		}
	}

	/**
	 * 根據用途決定郵件標題
	 */
	private String getSubject(String purpose) {
		switch (purpose) {
		case "register":
			return "【SKJL Calendar】歡迎註冊 - Email 驗證碼";
		case "reset":
			return "【SKJL Calendar】重設密碼驗證";
		case "change":
			return "【SKJL Calendar】安全驗證 - 修改密碼";
		default:
			return "【SKJL Calendar】驗證碼通知";
		}
	}

	/**
	 * 根據用途決定內文顯示的文字
	 */
	private String getPurposeName(String purpose) {
		switch (purpose) {
		case "register":
			return "註冊帳號";
		case "reset":
			return "重設密碼";
		case "change":
			return "修改密碼";
		default:
			return "安全驗證";
		}
	}
}