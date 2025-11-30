<template>
  <header class="workspace-header">
    <div class="left-section">
      <div class="back-btn" @click="$emit('back')">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="logo">
        <span class="logo-icon">✦</span>
        <span class="logo-text">结构标注 / 智能标注</span>
      </div>
    </div>
    
    <div class="center-section">
      <div 
        v-for="tab in tabs" 
        :key="tab.id"
        class="nav-tab"
        :class="{ active: currentTab === tab.id }"
        @click="$emit('update:currentTab', tab.id)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="right-section">
      <el-button circle text class="icon-btn">
        <el-icon><Setting /></el-icon>
      </el-button>
      <el-button circle text class="icon-btn">
        <el-icon><Search /></el-icon>
      </el-button>
      <el-button circle text class="icon-btn">
        <el-icon><Bell /></el-icon>
      </el-button>
      <el-button circle text class="icon-btn">
        <el-icon><QuestionFilled /></el-icon>
      </el-button>
      <div class="avatar">
        <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { Setting, Search, Bell, QuestionFilled, ArrowLeft } from '@element-plus/icons-vue';

defineProps({
  currentTab: {
    type: String,
    required: true
  }
});

defineEmits(['update:currentTab', 'back']);

const tabs = [
  { id: 'structure', label: '结构标注' },
  { id: 'entity', label: '实体标注' },
  { id: 'relation', label: '关系标注' },
  { id: 'graph', label: '知识图谱' },
  { id: 'export', label: '导出数据' }
];
</script>

<style scoped>
.workspace-header {
  height: 60px;
  background: #FFFFFF;
  border-bottom: 1px solid #EDEEF0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-sizing: border-box;
  flex-shrink: 0;
}

.left-section {
  display: flex;
  align-items: center;
  min-width: 200px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  margin-right: 8px;
  color: #596274;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #F3F4F6;
  color: #1F2328;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1F2328;
}

.logo-icon {
  color: #3B82F6;
  font-size: 20px;
}

.center-section {
  display: flex;
  gap: 8px;
  background: #F3F4F6;
  padding: 4px;
  border-radius: 999px;
}

.nav-tab {
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 14px;
  color: #596274;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.nav-tab:hover {
  color: #1F2328;
}

.nav-tab.active {
  background: #FFFFFF;
  color: #3B82F6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  font-weight: 500;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
  justify-content: flex-end;
}

.icon-btn {
  color: #596274;
  font-size: 18px;
}

.icon-btn:hover {
  color: #1F2328;
  background: #F3F4F6;
}

.avatar {
  margin-left: 12px;
  cursor: pointer;
}
</style>
