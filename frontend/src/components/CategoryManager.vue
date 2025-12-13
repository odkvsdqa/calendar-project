<template>
  <BaseModal
    :show="true"
    :title="$t('event.manageCategories')"
    width="500px"
    @close="emit('close')"
  >
    <div class="category-manager">
      <!-- 系統預設類型 -->
      <div class="section">
        <h4>{{ $t("category.system") }}</h4>
        <div class="category-list">
          <div
            v-for="category in systemCategories"
            :key="category.id"
            class="category-item system"
          >
            <span class="category-icon">{{ category.icon }}</span>
            <span class="category-name">{{
              getCategoryDisplayName(category)
            }}</span>
            <div
              class="category-color"
              :style="{ background: category.color }"
            ></div>
          </div>
        </div>
      </div>

      <!-- 自訂類型 -->
      <div class="section">
        <div class="section-header">
          <h4>{{ $t("category.custom") }}</h4>
          <button class="btn-add" @click="openAddModal">
            + {{ $t("category.add") }}
          </button>
        </div>
        <div class="category-list">
          <div
            v-for="category in customCategories"
            :key="category.id"
            class="category-item"
          >
            <span class="category-icon">{{ category.icon }}</span>
            <span class="category-name">{{ category.name }}</span>
            <div
              class="category-color"
              :style="{ background: category.color }"
            ></div>
            <div class="actions">
              <button class="btn-icon" @click="editCategory(category)">
                ✏️
              </button>
              <button class="btn-icon" @click="confirmDelete(category)">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BaseModal>

  <!-- 新增/編輯類型彈窗 -->
  <BaseModal
    v-if="showEditModal"
    :show="true" 
    :title="editingCategory ? $t('category.edit') : $t('category.add')"
    width="350px"
    @close="closeEditModal"
  >
    <form @submit.prevent="saveCategory">
      <div class="form-group">
        <label>{{ $t("category.name") }}</label>
        <input v-model="categoryForm.name" class="input-styled" required />
      </div>

      <div class="form-group">
        <label>{{ $t("category.icon") }}</label>
        <div class="icon-picker">
          <div
            v-for="icon in iconOptions"
            :key="icon"
            class="icon-option"
            :class="{ active: categoryForm.icon === icon }"
            @click="categoryForm.icon = icon"
          >
            {{ icon }}
          </div>
        </div>
      </div>

      <div class="form-group">
        <label>{{ $t("category.color") }}</label>
        <div class="color-picker">
          <div
            v-for="color in colorOptions"
            :key="color"
            class="color-option"
            :class="{ active: categoryForm.color === color }"
            :style="{ background: color }"
            @click="categoryForm.color = color"
          >
            <span v-if="categoryForm.color === color" class="check">✓</span>
          </div>
        </div>
      </div>

      <div class="modal-actions">
        <button type="button" class="btn-cancel" @click="closeEditModal">
          {{ $t("common.cancel") }}
        </button>
        <button type="submit" class="btn-save">
          {{ $t("common.save") }}
        </button>
      </div>
    </form>
  </BaseModal>

  <!-- 刪除確認 -->
  <BaseModal
    v-if="showDeleteConfirm"
    :show="true" 
    :title="$t('category.delete')"
    width="320px"
    @close="showDeleteConfirm = false"
  >
    <p style="text-align: center; color: #666; margin-bottom: 20px">
      {{ $t("category.confirmDelete") }}
    </p>
    <div class="confirm-actions">
      <button class="btn-cancel" @click="showDeleteConfirm = false">
        {{ $t("common.cancel") }}
      </button>
      <button class="btn-confirm" @click="handleDelete">
        {{ $t("common.confirm") }}
      </button>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import BaseModal from "./common/BaseModal.vue";
import { useCategories } from "../composables/useCategories";
import { useToast } from "../composables/useToast";

const { t } = useI18n();
const emit = defineEmits(["close","updated"]);
const { showToast } = useToast();

const {
  categories,
  loadCategories,
  getCategoryDisplayName,
  createCategory,
  updateCategory,
  deleteCategory,
} = useCategories();

// 分類顯示
const systemCategories = computed(() =>
  categories.value.filter((c) => c.isSystem)
);
const customCategories = computed(() =>
  categories.value.filter((c) => !c.isSystem)
);

// 編輯相關
const showEditModal = ref(false);
const editingCategory = ref(null);
const categoryForm = ref({
  name: "",
  icon: "📌",
  color: "#557c55",
});

// 圖標選項
const iconOptions = [
  "⚽",
  "💪",
  "💼",
  "🎬",
  "📚",
  "👥",
  "🍔",
  "✈️",
  "🎮",
  "🎵",
  "🏠",
  "💡",
];

// 顏色選項
const colorOptions = [
  "#4CAF50",
  "#FF9800",
  "#2196F3",
  "#E91E63",
  "#9C27B0",
  "#FF5722",
  "#009688",
  "#795548",
];

// 刪除確認
const showDeleteConfirm = ref(false);
const deletingCategory = ref(null);

// 方法
const openAddModal = () => {
  editingCategory.value = null;
  categoryForm.value = {
    name: "",
    icon: "📌",
    color: "#557c55",
  };
  showEditModal.value = true;
};

const editCategory = (category) => {
  editingCategory.value = category;
  categoryForm.value = {
    name: category.name,
    icon: category.icon,
    color: category.color,
  };
  showEditModal.value = true;
};

const closeEditModal = () => {
  showEditModal.value = false;
  editingCategory.value = null;
};

const saveCategory = async () => {
  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, categoryForm.value);
      showToast(t("messages.saveSuccess"), "success");
    } else {
      await createCategory(categoryForm.value);
      showToast(t("messages.saveSuccess"), "success");
    }
    //修正：操作成功後發出 updated 事件，讓父層更新下拉選單
    emit("updated");
    closeEditModal();
  } catch (error) {
    showToast(t("errors.operationFailed"), "error");
  }
};

const confirmDelete = (category) => {
  deletingCategory.value = category;
  showDeleteConfirm.value = true;
};

const handleDelete = async () => {
  try {
    await deleteCategory(deletingCategory.value.id);
    showToast(t("messages.deleteSuccess"), "success");
    // 修正：刪除成功後發出 updated 事件
    emit("updated");
    showDeleteConfirm.value = false;
    deletingCategory.value = null;
  } catch (error) {
    showToast(t("errors.deleteFailed"), "error");
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.category-manager {
  padding: 10px 0;
}
.section {
  margin-bottom: 30px;
}
.section h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.btn-add {
  padding: 6px 12px;
  background: #557c55;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}
.btn-add:hover {
  background: #446344;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  transition: background 0.2s;
}
.category-item:hover {
  background: #f0f0f0;
}
.category-item.system {
  opacity: 0.8;
}
.category-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}
.category-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}
.category-color {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 1px #ddd;
}
.actions {
  display: flex;
  gap: 5px;
}
.btn-icon {
  background: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}
.btn-icon:hover {
  background: #e0e0e0;
}

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
}
.input-styled:focus {
  outline: none;
  border-color: #557c55;
}

.icon-picker {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}
.icon-option {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  border: 2px solid transparent;
  transition: all 0.2s;
}
.icon-option:hover {
  background: #f0f0f0;
}
.icon-option.active {
  border-color: #557c55;
  background: #e8f5e9;
}

.color-picker {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
  border-color: #333;
}
.check {
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}
.btn-cancel {
  padding: 8px 16px;
  background: #f3f4f6;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #e5e7eb;
}
.btn-save {
  padding: 8px 16px;
  background: #557c55;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-save:hover {
  background: #446344;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.btn-confirm {
  padding: 10px 20px;
  background: #333;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
