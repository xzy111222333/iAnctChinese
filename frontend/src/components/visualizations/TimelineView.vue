<template>
  <div class="timeline-container">
    <div class="timeline-bg"></div>
    <div class="timeline-header">
      <h3>发展历程</h3>
      <span class="sub">生平节点 / 行军路标 / 事件脉络</span>
      <div class="timeline-controls">
        <el-button size="small" @click="expandAll">全部展开</el-button>
        <el-button size="small" @click="collapseAll">全部收起</el-button>
      </div>
    </div>

    
    <el-dialog
      v-model="dialogVisible"
      :title="selectedMilestone?.title"
      width="700px"
      :close-on-click-modal="true"
      class="milestone-dialog"
      align-center
      @close="closeDialog"
    >
      <div v-if="selectedMilestone" class="dialog-content">
        
        <div class="detail-grid">
          <div class="detail-card">
            <div class="card-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">时间</div>
              <div class="card-value">{{ selectedMilestone.dateLabel || '未注明' }}</div>
            </div>
          </div>

          <div class="detail-card">
            <div class="card-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">重要程度</div>
              <div class="card-value">
                <el-rate 
                  v-model="selectedMilestone.significance" 
                  disabled 
                  :max="5"
                  class="significance-rate"
                />
              </div>
            </div>
          </div>

          
          <div class="detail-card">
            <div class="card-icon">
              <el-icon><Location /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">发生地点</div>
              <div class="card-value">{{ selectedMilestone.location || '未注明' }}</div>
            </div>
          </div>
        </div>

        
        <div class="detail-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            事件描述
          </div>
          <div class="section-content description-text">
            {{ selectedMilestone.description }}
          </div>
        </div>

       
        <div class="detail-section">
          <div class="section-title">
            <el-icon><User /></el-icon>
            相关人物
          </div>
          <div class="section-content">
            <div class="tags-container">
              <el-tag
                v-for="person in selectedMilestone.participants"
                :key="person"
                class="person-tag"
                :style="{ background: pickColor(currentIndex) }"
              >
                {{ person }}
              </el-tag>
              
              <div v-if="!selectedMilestone.participants || selectedMilestone.participants.length === 0" class="no-data">
                暂无相关人物信息
              </div>
            </div>
          </div>
        </div>

        
        <div class="detail-section">
          <div class="section-title">
            <el-icon><TrendCharts /></el-icon>
            历史影响
          </div>
          <div class="section-content impact-text">
            {{ selectedMilestone.impact || '暂无影响说明' }}
          </div>
        </div>

       
        <div class="detail-section">
          <div class="section-title">
            <el-icon><Connection /></el-icon>
            关联事件
          </div>
          <div class="section-content">
            <ul class="related-list">
              <li v-for="(event, idx) in selectedMilestone.relatedEvents" :key="idx">
                {{ event }}
              </li>
             
              <li v-if="!selectedMilestone.relatedEvents || selectedMilestone.relatedEvents.length === 0" class="no-data">
                暂无关联事件
              </li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button 
            @click="prevMilestone" 
            :disabled="currentIndex === 0"
            class="nav-button"
            :style="{ borderColor: pickColor(currentIndex), color: pickColor(currentIndex) }"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一个
          </el-button>
          
          <div class="page-indicator">
            {{ currentIndex + 1 }} / {{ milestones.length }}
          </div>
          
          <el-button 
            @click="nextMilestone" 
            :disabled="currentIndex === milestones.length - 1"
            class="nav-button"
            :style="{ borderColor: pickColor(currentIndex), color: pickColor(currentIndex) }"
          >
            下一个
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          
          <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <div class="timeline-axis">
      <div class="axis-line"></div>
      <transition-group name="card-fade" tag="div" class="timeline-cards">
        <div
          v-for="(milestone, index) in milestones"
          :key="milestone.title + index"
          :class="['timeline-card', { 'expanded': expandedCards[index], 'highlight': hoveredIndex === index }]"
          @mouseenter="hoveredIndex = index"
          @mouseleave="hoveredIndex = null"
        >
          
          <div 
            class="node" 
            :style="{ borderColor: pickColor(index), background: pointBg(index) }"
            @click="showDetails(milestone, index)"
          >
            <span class="node-label">{{ milestone.dateLabel }}</span>
            <div class="node-pulse" :style="{ borderColor: pickColor(index) }"></div>
          </div>

        
          <transition name="expand">
            <div 
              class="card-content" 
              :style="{ borderColor: pickColor(index) }"
              @click="toggleCard(index)"
            >
              <div class="card-head">
                <span class="date">{{ milestone.dateLabel || "未注明" }}</span>
                <div class="card-actions">
                  <span class="badge" :style="{ background: pickColor(index) }">
                    {{ milestone.significance || 1 }}
                  </span>
                  <el-icon class="expand-icon" :class="{ 'rotated': expandedCards[index] }">
                    <ArrowDown />
                  </el-icon>
                </div>
              </div>
              <h4>{{ milestone.title }}</h4>
              <p class="brief">{{ milestone.description }}</p>
              
              
              <transition name="slide-down">
                <div v-if="expandedCards[index]" class="expanded-content">
                 
                  <div class="extra-info">
                    <el-icon><Location /></el-icon>
                    <span>{{ milestone.location || '地点未注明' }}</span>
                  </div>
                  
                  <div class="extra-info">
                    <el-icon><User /></el-icon>
                    <span>{{ (milestone.participants && milestone.participants.length > 0) ? milestone.participants.join('、') : '暂无相关人物' }}</span>
                  </div>
                  
                  <div class="impact-text">
                    <strong>影响：</strong>{{ milestone.impact || '暂无影响说明' }}
                  </div>
                  <el-button 
                    type="primary" 
                    size="small" 
                    class="detail-btn"
                    @click.stop="showDetails(milestone, index)"
                  >
                    查看完整详情
                  </el-button>
                </div>
              </transition>
            </div>
          </transition>
        </div>
      </transition-group>
    </div>

   
    <div class="timeline-navigator-center" v-if="milestones.length > 0">
      <div class="nav-title">时序导航</div>
      <div class="nav-dots">
        <div
          v-for="(milestone, index) in milestones"
          :key="index"
          :class="['nav-dot', { active: currentIndex === index }]"
          :style="{ background: pickColor(index) }"
          @click="scrollToMilestone(index)"
          :title="milestone.title"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { 
  ArrowLeft, 
  ArrowRight, 
  ArrowDown, 
  Location, 
  User,
  Calendar,
  Star,
  Document,
  TrendCharts,
  Connection
} from '@element-plus/icons-vue';

const props = defineProps({
  milestones: {
    type: Array,
    default: () => []
  }
});

const dialogVisible = ref(false);
const selectedMilestone = ref(null);
const currentIndex = ref(0);
const expandedCards = reactive({});
const hoveredIndex = ref(null);

const pickColor = (idx) => {
  const colors = ["#d16a5d", "#3a7a87", "#a58938", "#6b6fb5", "#c06d9d"];
  return colors[idx % colors.length];
};

const pointBg = (idx) => {
  const color = pickColor(idx);
  return `radial-gradient(circle at center, ${color} 0%, ${color} 35%, transparent 60%)`;
};


const showDetails = (milestone, index) => {
  selectedMilestone.value = milestone;
  currentIndex.value = index;
  dialogVisible.value = true;
};


const closeDialog = () => {
  dialogVisible.value = false;
};


const toggleCard = (index) => {
  expandedCards[index] = !expandedCards[index];
};


const expandAll = () => {
  props.milestones.forEach((_, index) => {
    expandedCards[index] = true;
  });
};


const collapseAll = () => {
  props.milestones.forEach((_, index) => {
    expandedCards[index] = false;
  });
};


const prevMilestone = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    selectedMilestone.value = props.milestones[currentIndex.value];
  }
};


const nextMilestone = () => {
  if (currentIndex.value < props.milestones.length - 1) {
    currentIndex.value++;
    selectedMilestone.value = props.milestones[currentIndex.value];
  }
};


const scrollToMilestone = (index) => {
  currentIndex.value = index;
  selectedMilestone.value = props.milestones[index];
  dialogVisible.value = true;
  
  const cards = document.querySelectorAll('.timeline-card');
  if (cards[index]) {
    cards[index].scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' });
   
    setTimeout(() => {
      expandedCards[index] = true;
    }, 500);
  }
};
</script>

<style scoped>
.timeline-container {
  position: relative;
  overflow-x: auto;
  padding: 28px 16px 80px;
  min-height: 420px;
}

.timeline-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(243, 229, 204, 0.6), rgba(202, 220, 236, 0.6));
  border-radius: 24px;
  filter: blur(20px);
  z-index: 0;
}

.timeline-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-left: 6px;
  flex-wrap: wrap;
}

.timeline-header h3 {
  margin: 0;
  color: #2f2b2a;
  font-weight: 700;
  letter-spacing: 1px;
}

.timeline-header .sub {
  color: #8a8178;
  font-size: 13px;
}

.timeline-controls {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.timeline-axis {
  position: relative;
  z-index: 1;
  min-width: 640px;
  padding: 16px 12px 24px;
}

.timeline-cards {
  display: flex;
  gap: 32px;
}

.axis-line {
  position: absolute;
  top: 92px;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #edc783, #9ac3ea);
  border-radius: 999px;
}

.timeline-card {
  flex: 1;
  min-width: 200px;
  position: relative;
  padding-top: 84px;
  transition: all 0.3s ease;
}

.timeline-card.highlight {
  transform: translateY(-4px);
}

.timeline-card::after {
  content: "";
  position: absolute;
  top: 95px;
  left: 50%;
  width: 2px;
  height: 40px;
  background: rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.timeline-card.highlight::after {
  height: 48px;
  background: rgba(0, 0, 0, 0.2);
}

.node {
  position: absolute;
  top: 48px;
  left: 50%;
  transform: translateX(-50%);
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 4px solid #f3b176;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #2f2b2a;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(8px);
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 2;
}

.node:hover {
  transform: translateX(-50%) scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.node-label {
  text-align: center;
  line-height: 1.2;
  padding: 4px;
}

.node-pulse {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px solid;
  animation: pulse 2s ease-in-out infinite;
  opacity: 0;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}

.card-content {
  background: white;
  border-radius: 16px;
  padding: 14px 18px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
}

.card-content:hover {
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-content h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #2f2b2a;
  font-weight: 600;
}

.card-content .brief {
  margin: 0;
  font-size: 13px;
  color: #6a645f;
  line-height: 1.5;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #8a8178;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date {
  font-weight: 700;
}

.badge {
  color: #fff;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
}

.expand-icon {
  transition: transform 0.3s ease;
  font-size: 16px;
  color: #8a8178;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

.expanded-content {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e0dbd5;
}

.extra-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6a645f;
}

.extra-info .el-icon {
  color: #a58938;
}

.impact-text {
  font-size: 13px;
  color: #6a645f;
  line-height: 1.6;
  margin-top: 8px;
  padding: 8px;
  background: rgba(165, 137, 56, 0.08);
  border-radius: 8px;
}

.detail-btn {
  margin-top: 12px;
  width: 100%;
}


.milestone-dialog {
  --el-dialog-bg-color: #fdfbf7;
}

.milestone-dialog .dialog-content {
  padding: 8px 0;
  max-height: 60vh;
  overflow-y: auto;
}


.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.detail-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border-left: 4px solid v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-icon {
  color: v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
  font-size: 20px;
}

.card-label {
  font-size: 12px;
  color: #9b8b7a;
  font-weight: 600;
  margin-bottom: 4px;
}

.card-value {
  font-size: 14px;
  color: #2c2416;
  font-weight: 500;
}

.significance-rate {
  --el-rate-icon-size: 16px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
  margin-bottom: 12px;
}

.detail-label {
  font-weight: 600;
  color: #2f2b2a;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-value {
  color: #6a645f;
  font-size: 14px;
  line-height: 1.6;
}

.detail-value.description {
  padding: 12px;
  background: #f9f7f4;
  border-radius: 8px;
  border-left: 4px solid #a58938;
}

.section-content {
  font-size: 14px;
  color: #2c2416;
  line-height: 1.6;
}

.description-text {
  padding: 16px;
  background: linear-gradient(135deg, #fef9f0 0%, #fdf5e8 100%);
  border-radius: 8px;
  border-left: 4px solid v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
}

.impact-text {
  padding: 16px;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
  border-radius: 8px;
  border-left: 4px solid v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.person-tag {
  color: white;
  border: none;
  border-radius: 16px;
  padding: 4px 12px;
}

.no-data {
  font-size: 13px;
  color: #8a8178;
  font-style: italic;
}

.related-list {
  margin: 0;
  padding-left: 20px;
}

.related-list li {
  margin-bottom: 6px;
  color: #6a645f;
}


.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 0;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid;
  background: white;
  border-radius: 24px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.nav-button:hover:not(:disabled) {
  background: v-bind('selectedMilestone ? pickColor(currentIndex) : "#a58938"');
  color: white;
  transform: translateY(-2px);
}

.nav-button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-indicator {
  font-size: 14px;
  color: #6b5d4f;
  font-weight: 600;
}


.timeline-navigator-center {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px 24px;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(184, 145, 46, 0.2);
}

.nav-title {
  font-size: 12px;
  color: #8a8178;
  margin-bottom: 8px;
  text-align: center;
  font-weight: 600;
}

.nav-dots {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.nav-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.4;
}

.nav-dot:hover {
  opacity: 0.8;
  transform: scale(1.3);
}

.nav-dot.active {
  opacity: 1;
  transform: scale(1.4);
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}


.expand-enter-active, .expand-leave-active {
  transition: all 0.3s ease;
}

.expand-enter-from, .expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
}

.slide-down-enter-from, .slide-down-leave-to {
  max-height: 0;
  opacity: 0;
}

.card-fade-enter-active, .card-fade-leave-active {
  transition: all 0.5s ease;
}

.card-fade-enter-from, .card-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>