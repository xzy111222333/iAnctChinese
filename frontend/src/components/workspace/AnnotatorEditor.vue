<template>
  <div class="annotator-editor">
    <div class="editor-header">
      <div class="filter-chips">
        <div 
          v-for="filter in filters" 
          :key="filter.key"
          class="chip"
          :class="{ active: activeFilters.includes(filter.key) }"
          @click="toggleFilter(filter.key)"
        >
          <span class="dot" :style="{ background: filter.color }"></span>
          {{ filter.label }}
          <span v-if="activeFilters.includes(filter.key)" class="close-icon">×</span>
        </div>
      </div>
      <div class="editor-actions">
         <el-button type="primary" link @click="$emit('runAnalysis')">
           <el-icon class="el-icon--left"><MagicStick /></el-icon> AI 智能标注
         </el-button>
         <div class="divider"></div>
         <el-button-group>
           <el-button :icon="Back" size="small" text />
           <el-button :icon="Right" size="small" text />
         </el-button-group>
      </div>
    </div>

    <div class="editor-canvas-container">
       <div class="editor-canvas" ref="canvasRef" @mouseup="handleTextSelection">
         <div v-if="!content" class="empty-state">请选择或上传文档</div>
         <div v-else class="text-content" v-html="renderedContent" @click="handleEntityClick"></div>
       </div>
    </div>

    <!-- Popover for Entity Info -->
    <div 
      v-if="activeEntity" 
      class="entity-popover"
      :style="{ top: popoverPosition.top + 'px', left: popoverPosition.left + 'px' }"
    >
      <div class="popover-header">
        <span class="popover-type" :style="{ color: getCategoryColor(activeEntity.category) }">
          {{ getCategoryLabel(activeEntity.category) }}
        </span>
        <span class="popover-conf">Confidence: {{ (activeEntity.confidence * 100).toFixed(0) }}%</span>
      </div>
      <div class="popover-label">{{ activeEntity.label }}</div>
      <div class="popover-actions">
        <button class="action-btn" @click="removeEntity(activeEntity.id)">删除</button>
        <button class="action-btn primary">编辑</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { Back, Right, MagicStick } from '@element-plus/icons-vue';

const props = defineProps({
  content: { type: String, default: '' },
  entities: { type: Array, default: () => [] }
});

const emit = defineEmits(['addEntity', 'removeEntity', 'runAnalysis']);

const filters = [
  { key: 'PERSON', label: '人物', color: '#3B82F6' },
  { key: 'LOCATION', label: '地点', color: '#06B6D4' },
  { key: 'TEMPORAL', label: '时间', color: '#8B5CF6' },
  { key: 'EVENT', label: '事件', color: '#10B981' },
  { key: 'ORGANIZATION', label: '组织', color: '#F59E0B' },
  { key: 'OBJECT', label: '器物', color: '#F59E0B' },
];

const activeFilters = ref(['PERSON', 'LOCATION', 'TEMPORAL', 'EVENT', 'ORGANIZATION', 'OBJECT']);
const activeEntity = ref(null);
const popoverPosition = ref({ top: 0, left: 0 });

const toggleFilter = (key) => {
  if (activeFilters.value.includes(key)) {
    activeFilters.value = activeFilters.value.filter(k => k !== key);
  } else {
    activeFilters.value.push(key);
  }
};

const getCategoryColor = (key) => {
  const f = filters.find(f => f.key === key);
  return f ? f.color : '#999';
};

const getCategoryLabel = (key) => {
  const f = filters.find(f => f.key === key);
  return f ? f.label : key;
};

// Simple text rendering with non-overlapping entities
// For overlapping, we'd need a more complex tree builder.
const renderedContent = computed(() => {
  if (!props.content) return '';
  
  const text = props.content;
  const sortedEntities = [...props.entities]
    .filter(e => activeFilters.value.includes(e.category))
    .sort((a, b) => a.startOffset - b.startOffset);
  
  let result = '';
  let lastIndex = 0;

  for (const entity of sortedEntities) {
    if (entity.startOffset < lastIndex) continue; // Skip overlapping for now

    // Text before entity
    result += text.slice(lastIndex, entity.startOffset);

    // Entity
    const color = getCategoryColor(entity.category);
    // Using inline styles for dynamic colors, but classes for glow effects
    result += `<span class="entity-highlight" data-id="${entity.id}" style="--entity-color: ${color}">${text.slice(entity.startOffset, entity.endOffset)}</span>`;
    
    lastIndex = entity.endOffset;
  }

  result += text.slice(lastIndex);
  
  // Convert newlines to breaks for display
  return result.replace(/\n/g, '<br/>');
});

const handleTextSelection = () => {
  const selection = window.getSelection();
  if (!selection.rangeCount || selection.isCollapsed) return;

  const range = selection.getRangeAt(0);
  // Calculate offset in original text... this is tricky with rendered HTML.
  // For a prototype, we might just emit the selected text and let the parent handle matching/guessing offset
  // Or simpler: The user highlights text, we popup a "Add Entity" dialog.
  // Ideally, we need to map DOM offset back to Model offset.
  
  // For now, let's just log or simplified action
  // console.log("Selected: ", selection.toString());
};

const handleEntityClick = (e) => {
  const target = e.target.closest('.entity-highlight');
  if (target) {
    const id = target.getAttribute('data-id');
    const entity = props.entities.find(en => String(en.id) === id);
    if (entity) {
      activeEntity.value = entity;
      // Position popover
      const rect = target.getBoundingClientRect();
      // Calculate relative to viewport or editor
      // We will use fixed positioning for simplicity relative to window, or offset relative to offsetParent
      // Let's assume fixed for now to handle scroll easily
      popoverPosition.value = {
        top: rect.bottom + 8, // below
        left: rect.left
      };
    }
  } else {
    activeEntity.value = null;
  }
};

const removeEntity = (id) => {
  emit('removeEntity', id);
  activeEntity.value = null;
};
</script>

<style scoped>
.annotator-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  position: relative;
  overflow: hidden;
}

.editor-header {
  padding: 16px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #EDEEF0;
  background: #fff;
  z-index: 10;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.divider {
  width: 1px;
  height: 16px;
  background: #E5E7EB;
}

.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #E5E7EB;
  background: #F9FAFB;
  font-size: 13px;
  color: #596274;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.chip:hover {
  background: #F3F4F6;
}

.chip.active {
  background: #E8F0FE; /* Fallback or light blue */
  border-color: #3B82F6;
  color: #1F2328;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.close-icon {
  margin-left: 4px;
  font-size: 14px;
  color: #9CA3AF;
}

.chip.active .close-icon {
  color: #3B82F6;
}

.editor-canvas-container {
  flex: 1;
  overflow-y: auto;
  padding: 40px 60px;
  background: #FAFAFB;
}

.editor-canvas {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  min-height: 800px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  padding: 48px;
  border-radius: 2px;
}

.text-content {
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 18px;
  line-height: 1.8;
  color: #1F2328;
  white-space: pre-wrap;
}

/* GLOWING HIGHLIGHT STYLE */
:deep(.entity-highlight) {
  position: relative;
  cursor: pointer;
  border-bottom: 2px solid var(--entity-color);
  background-color: color-mix(in srgb, var(--entity-color) 10%, transparent);
  border-radius: 4px;
  padding: 0 2px;
  transition: all 0.2s ease;
  
  /* Initial subtle glow */
  box-shadow: 0 0 0 0 rgba(var(--entity-color), 0);
}

:deep(.entity-highlight:hover) {
  /* Stronger bg */
  background-color: color-mix(in srgb, var(--entity-color) 20%, transparent);
  
  /* Glowing effect */
  box-shadow: 
    0 0 8px 2px color-mix(in srgb, var(--entity-color) 40%, transparent),
    inset 0 0 4px color-mix(in srgb, var(--entity-color) 20%, transparent);
  
  text-shadow: 0 0 1px var(--entity-color);
  z-index: 1;
}

.entity-popover {
  position: fixed;
  width: 200px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid #E5E7EB;
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  padding: 12px;
  z-index: 100;
  transform: translate(-50%, 0); /* Center horizontally relative to left */
}

.popover-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
}

.popover-type {
  font-weight: 600;
}

.popover-conf {
  color: #9CA3AF;
}

.popover-label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #111827;
}

.popover-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 4px;
  border: 1px solid #E5E7EB;
  background: #fff;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.1s;
}

.action-btn:hover {
  background: #F9FAFB;
}

.action-btn.primary {
  background: #3B82F6;
  color: #fff;
  border-color: #3B82F6;
}
.action-btn.primary:hover {
  background: #2563EB;
}

.empty-state {
  color: #9CA3AF;
  text-align: center;
  margin-top: 100px;
}
</style>
