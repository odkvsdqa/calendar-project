<!-- src/components/EventModal.vue -->
<template>
  <BaseModal
    :show="true"
    :title="modalTitle"
    width="450px"
    @close="handleClose"
  >
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <!-- 標題 -->
        <label>{{ $t("event.title") }}</label>
        <input
          type="text"
          v-model="localForm.title"
          required
          :placeholder="$t('event.placeholder.title')"
          class="input-styled"
        />
      </div>

      <div class="form-group">
        <!-- 🔥 預計花費（含幣別選擇）-->
        <label>{{ $t("event.cost") }}</label>
        <div class="cost-input-group">
          <!-- 幣別選擇下拉 -->
          <select v-model="localForm.currency" class="currency-select">
            <option value="TWD">NT$</option>
            <option value="USD">$</option>
            <option value="JPY">¥</option>
            <option value="EUR">€</option>
            <option value="CNY">¥</option>
            <option value="KRW">₩</option>
          </select>

          <!-- 金額輸入框 -->
          <input
            type="number"
            v-model="localForm.estimatedCost"
            min="0"
            :placeholder="$t('event.placeholder.cost')"
            class="input-styled cost-input"
          />
        </div>
      </div>

      <div class="form-group">
        <!-- 描述 -->
        <label>{{ $t("event.description") }}</label>
        <textarea
          v-model="localForm.description"
          rows="3"
          :placeholder="$t('event.placeholder.desc')"
          class="input-styled"
        ></textarea>
      </div>

      <div class="form-row">
        <div class="form-group half">
          <!-- 開始時間 -->
          <label>{{ $t("event.start") }}</label>
          <input
            type="datetime-local"
            v-model="localForm.startTime"
            required
            class="input-styled"
          />
        </div>
        <div class="form-group half">
          <!-- 結束時間 -->
          <label>{{ $t("event.end") }}</label>
          <input
            type="datetime-local"
            v-model="localForm.endTime"
            required
            class="input-styled"
          />
        </div>
      </div>

      <div class="form-group">
        <!-- 顏色選擇 -->
        <label>{{ $t("event.color") }}</label>
        <div class="color-picker">
          <div
            v-for="color in colorOptions"
            :key="color.value"
            class="color-option"
            :class="{ active: localForm.color === color.value }"
            :style="{ backgroundColor: color.value }"
            @click="localForm.color = color.value"
          >
            <span v-if="localForm.color === color.value" class="check">✓</span>
          </div>
        </div>
      </div>

      <div class="form-footer">
        <!-- 左側刪除按鈕 -->
        <button
          v-if="localForm.id"
          type="button"
          class="btn-delete"
          @click="handleDeleteClick"
        >
          {{ $t("common.delete") }}
        </button>
        <div v-else></div>

        <!-- 右側操作按鈕 -->
        <div class="action-buttons">
          <button type="button" class="btn-cancel" @click="handleClose">
            {{ $t("common.cancel") }}
          </button>
          <button type="submit" class="btn-save">
            {{ $t("common.save") }}
          </button>
        </div>
      </div>
    </form>
  </BaseModal>

  <!-- 刪除確認視窗 -->
  <BaseModal
    :show="showDeleteConfirm"
    :title="$t('event.deleteConfirmTitle')"
    width="320px"
    @close="showDeleteConfirm = false"
  >
    <div class="confirm-content">
      <p>{{ $t("event.deleteConfirmText") }}</p>
      <div class="confirm-actions">
        <button class="btn-cancel" @click="showDeleteConfirm = false">
          {{ $t("common.cancel") }}
        </button>
        <button class="btn-delete-confirm" @click="confirmDelete">
          {{ $t("common.confirm") }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import { useI18n } from "vue-i18n";
import BaseModal from "./common/BaseModal.vue";

const { t } = useI18n();

const props = defineProps({
  eventForm: Object,
  modalTitle: { type: String, default: "" },
});
const emit = defineEmits(["close", "save", "delete"]);
const showDeleteConfirm = ref(false);

const colorOptions = [
  { value: "#557c55", name: "森綠" },
  { value: "#7c8db5", name: "霧藍" },
  { value: "#bfaac1", name: "藕紫" },
  { value: "#b0a496", name: "亞麻" },
];

const localForm = ref({
  id: null,
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  color: "#557c55",
  estimatedCost: null,
  currency: "TWD", // 🔥 預設新台幣
});

const modalTitle = computed(() => {
  if (props.modalTitle) return props.modalTitle;
  return localForm.value.id ? t("event.editTitle") : t("event.addTitle");
});

const syncFormData = () => {
  if (props.eventForm) {
    localForm.value = {
      ...props.eventForm,
      color: props.eventForm.color || "#557c55",
      estimatedCost: props.eventForm.estimatedCost || null,
      currency: props.eventForm.currency || "TWD", // 🔥 同步幣別
    };
  }
};

const handleClose = () => emit("close");
const handleSubmit = () => emit("save", localForm.value);
const handleDeleteClick = () => (showDeleteConfirm.value = true);
const confirmDelete = () => {
  showDeleteConfirm.value = false;
  emit("delete", localForm.value.id);
};

syncFormData();
watch(() => props.eventForm, syncFormData, { deep: true });
</script>

<style scoped>
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.input-styled {
  width: 100%;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  transition: border-color 0.2s;
  box-sizing: border-box;
  font-family: inherit;
}
.input-styled:focus {
  outline: none;
  border-color: #557c55;
}

/* 🔥 新增：金額輸入組（幣別 + 金額）*/
.cost-input-group {
  display: flex;
  gap: 8px;
}

.currency-select {
  width: 85px;
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.currency-select:focus {
  outline: none;
  border-color: #557c55;
}

.cost-input {
  flex: 1;
}

.form-row {
  display: flex;
  gap: 8px;
}
.half {
  flex: 1;
}

.color-picker {
  display: flex;
  gap: 10px;
}
.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
  transition: transform 0.2s;
}
.color-option:hover {
  transform: scale(1.1);
}
.color-option.active {
  border-color: #555;
}
.check {
  color: white;
  font-size: 14px;
}

.form-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.action-buttons {
  display: flex;
  gap: 10px;
}

button {
  padding: 8px 20px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  letter-spacing: 0.05em;
  transition: all 0.2s;
}
.btn-save {
  background: #557c55;
  color: white;
}
.btn-save:hover {
  background: #446344;
}
.btn-cancel {
  background: #f3f4f6;
  color: #666;
  border: 1px solid #e0e0e0;
}
.btn-cancel:hover {
  background: #e5e7eb;
}
.btn-delete {
  color: #d98e8e;
  background: transparent;
  padding-left: 0;
}
.btn-delete:hover {
  color: #c06060;
  text-decoration: underline;
}

.confirm-content {
  text-align: center;
}
.confirm-content p {
  color: #666;
  margin-bottom: 24px;
}
.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
.btn-delete-confirm {
  background: #d98e8e;
  color: white;
}
</style>
