package com.calendar.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "events", indexes = {
		// 為常用的查詢欄位建立索引
		@Index(name = "idx_event_user", columnList = "user_id"),
		// 注意：多欄位索引中間建議不要有空格，避免部分舊版 Hibernate 解析錯誤
		@Index(name = "idx_event_time", columnList = "start_time,end_time") })
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, name = "start_time")
	// @JsonFormat 已移除：日期格式化交由 DTO 或全域設定處理
	private LocalDateTime startTime;

	@Column(nullable = false, name = "end_time")
	private LocalDateTime endTime;

	@Column(length = 20)
	private String color;

	@ManyToOne(fetch = FetchType.LAZY) // 保持 Lazy Loading 優化效能
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore // 保留這個，防止 toString 或意外序列化時發生無窮迴圈
	private User user;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// 關聯：財務實體
	@OneToOne(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private EventFinancial financial;

	public Event() {
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
		if (color == null || color.isEmpty()) {
			color = "#667eea";
		}
		// 確保 financial 物件存在，避免 NullPointerException
		if (financial == null) {
			financial = new EventFinancial();
			financial.setEvent(this);
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	// === Getters and Setters ===

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * 設定預估金額（同步寫入 EventFinancial） 已移除 @JsonProperty 和 System.out.println -> 職責分離
	 */
	@Transient // 標記這不是 Event 表的欄位
	public void setEstimatedCost(BigDecimal cost) {
		if (this.financial == null) {
			this.financial = new EventFinancial();
			this.financial.setEvent(this);
		}
		this.financial.setEstimatedCost(cost);
	}

	/**
	 * 取得預估金額（從 EventFinancial 讀取）
	 */
	@Transient
	public BigDecimal getEstimatedCost() {
		return financial != null ? financial.getEstimatedCost() : BigDecimal.ZERO;
	}

	/**
	 * 取得幣別（從 EventFinancial）
	 */
	@Transient
	public String getCurrency() {
		return financial != null ? financial.getCurrency() : "TWD";
	}

	/**
	 * 設定幣別（同步到 EventFinancial）
	 */
	public void setCurrency(String currency) {
		if (this.financial == null) {
			this.financial = new EventFinancial();
			this.financial.setEvent(this);
		}
		this.financial.setCurrency(currency);
	}

	// 在現有的 Event.java 中新增：

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private EventCategory category;

	public EventCategory getCategory() {
		return category;
	}

	public void setCategory(EventCategory category) {
		this.category = category;
	}
}