<template>
  <aside class="entity-toolbox">
    <div class="toolbox-header">
      <h3 class="title">实体类目</h3>
      <el-button link type="primary" size="small">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <div class="category-list">
      <div 
        v-for="cat in categories" 
        :key="cat.key" 
        class="category-card"
        :class="{ active: selectedCategory === cat.key }"
        @click="selectCategory(cat.key)"
      >
        <div class="card-header">
          <div class="cat-info">
            <span class="color-dot" :style="{ background: cat.color }"></span>
            <span class="cat-name">{{ cat.label }}</span>
          </div>
          <el-tag size="small" effect="plain" round>{{ getCount(cat.key) }}</el-tag>
        </div>
        <div class="card-actions">
           <el-button link size="small" :icon="Edit" />
           <el-button link size="small" :icon="Plus" />
        </div>
      </div>
    </div>

    <div class="toolbox-footer">
      <el-button class="add-btn" plain>
        <el-icon><Plus /></el-icon> 添加实体类
      </el-button>
    </div>
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue';
import { Plus, Edit } from '@element-plus/icons-vue';

const props = defineProps({
  entities: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['selectCategory']);
const selectedCategory = ref(null);

const categories = [
  { key: 'PERSON', label: '人物', color: '#3B82F6' },
  { key: 'LOCATION', label: '地点', color: '#06B6D4' },
  { key: 'TEMPORAL', label: '时间', color: '#8B5CF6' },
  { key: 'EVENT', label: '事件', color: '#10B981' },
  { key: 'ORGANIZATION', label: '组织', color: '#F59E0B' },
  { key: 'OBJECT', label: '器物/典籍', color: '#F59E0B' }, // reusing Amber for Book/Object
];

const getCount = (key) => {
  return props.entities.filter(e => e.category === key).length;
};

const selectCategory = (key) => {
  selectedCategory.value = key;
  emit('selectCategory', key);
};
</script>

<style scoped>
.entity-toolbox {
  width: 280px;
  background: #FAFAFB;
  border-right: 1px solid #EDEEF0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.toolbox-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2328;
  margin: 0;
}

.category-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(17, 24, 39, 0.06);
}

.category-card.active {
  background: #E8F0FE;
  border-color: #3B82F6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cat-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.cat-name {
  font-size: 14px;
  font-weight: 500;
  color: #1F2328;
}

.card-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  justify-content: flex-end;
}

.category-card:hover .card-actions {
  opacity: 1;
}

.toolbox-footer {
  padding: 16px;
  border-top: 1px solid #EDEEF0;
}

.add-btn {
  width: 100%;
  border-style: dashed;
}
</style>
