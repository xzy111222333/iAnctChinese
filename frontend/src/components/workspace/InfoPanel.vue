<template>
  <aside class="info-panel">
    <div class="panel-header">
      <el-icon><Document /></el-icon>
      <span class="header-title">文档信息</span>
    </div>

    <div class="info-content" v-if="text">
      <div class="info-card">
        <div class="info-row">
          <label>ID</label>
          <div class="val mono">{{ text.id }}</div>
        </div>
        <div class="info-row">
          <label>标题</label>
          <div class="val">{{ text.title }}</div>
        </div>
         <div class="info-row">
          <label>作者</label>
          <div class="val">{{ text.author || '-' }}</div>
        </div>
         <div class="info-row">
          <label>时代</label>
          <div class="val">{{ text.era || '-' }}</div>
        </div>
        <div class="info-row">
          <label>类型</label>
          <div class="val"><el-tag size="small">{{ translateCategory(text.category) }}</el-tag></div>
        </div>
      </div>

      <div class="info-card stats-card">
        <div class="stat-item">
          <div class="stat-num">{{ entitiesCount }}</div>
          <div class="stat-label">实体</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ relationsCount }}</div>
          <div class="stat-label">关系</div>
        </div>
      </div>
      
       <div class="info-card">
        <div class="info-row">
           <label>更新时间</label>
           <div class="val sm">{{ formatDate(text.updatedAt) }}</div>
        </div>
      </div>

    </div>
    <div v-else class="empty-state">
      暂无文档信息
    </div>
  </aside>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue';

defineProps({
  text: { type: Object, default: null },
  entitiesCount: { type: Number, default: 0 },
  relationsCount: { type: Number, default: 0 }
});

const translateCategory = (category) => {
  const map = {
    warfare: "战争纪实",
    travelogue: "游记地理",
    biography: "人物传记",
    unknown: "待识别",
    other: "其他"
  };
  return map[category] || category || "未知";
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString();
};
</script>

<style scoped>
.info-panel {
  width: 260px;
  background: #FAFAFB;
  border-left: 1px solid #EDEEF0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  height: 50px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  color: #1F2328;
  border-bottom: 1px solid #EDEEF0;
}

.info-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

.info-card {
  background: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.info-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}
.info-row:last-child {
  margin-bottom: 0;
}

.info-row label {
  font-size: 12px;
  color: #8B95A7;
}

.info-row .val {
  font-size: 14px;
  color: #1F2328;
  font-weight: 500;
  word-break: break-all;
}

.info-row .val.mono {
  font-family: monospace;
  font-size: 12px;
  color: #596274;
}

.info-row .val.sm {
  font-size: 12px;
  color: #596274;
}

.stats-card {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-num {
  font-size: 20px;
  font-weight: 600;
  color: #3B82F6;
}

.stat-label {
  font-size: 12px;
  color: #8B95A7;
  margin-top: 4px;
}

.empty-state {
  padding: 20px;
  color: #9CA3AF;
  text-align: center;
  font-size: 13px;
}
</style>
