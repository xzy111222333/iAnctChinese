<template>
  <div class="family-tree">
    
    <div class="tree-header">
      <h3>家谱树</h3>
      <span class="subtitle">血脉相连 · 亲情永恒</span>
      <div class="tree-controls">
        <el-button size="small" @click="expandAll" class="control-btn">
          <el-icon><Expand /></el-icon>
          展开全部
        </el-button>
        <el-button size="small" @click="collapseAll" class="control-btn">
          <el-icon><Fold /></el-icon>
          收起全部
        </el-button>
      </div>
    </div>

    <div v-if="!hasData" class="empty-state">
      <div class="empty-icon">
        <el-icon><DataBoard /></el-icon>
      </div>
      <div class="empty-text">暂无家谱数据</div>
      <div class="empty-subtext">请添加家族成员信息</div>
    </div>
    
    
    <div v-else class="chart-enhanced-container">
      <div ref="chartRef" class="chart-enhanced"></div>
      <div class="family-legend">
        <div class="legend-item">
          <div class="legend-color male"></div>
          <span>男性成员</span>
        </div>
        <div class="legend-item">
          <div class="legend-color female"></div>
          <span>女性成员</span>
        </div>
        <div class="legend-item">
          <div class="legend-color other"></div>
          <span>其他关系</span>
        </div>
      </div>
    </div>

    
    <el-dialog
      v-model="dialogVisible"
      :title="selectedNode?.name"
      width="680px"
      :close-on-click-modal="true"
      class="family-dialog-enhanced"
      align-center
    >
      <div v-if="selectedNode" class="dialog-content-enhanced">
        
        <div class="member-header">
          <div class="member-avatar" :class="getGenderClass(selectedNode.name)">
            <el-icon><User /></el-icon>
          </div>
          <div class="member-basic-info">
            <div class="member-name">
              {{ selectedNode.name || '未知' }}
              <el-tag :type="getGenderType(selectedNode.name)" size="small" class="gender-tag">
                {{ getGenderText(selectedNode.name) }}
              </el-tag>
            </div>
            <div class="member-relation">{{ selectedNode.relation || '家族成员' }}</div>
          </div>
        </div>

        
        <div class="detail-grid">
          <div v-if="selectedNode.birthYear" class="detail-card">
            <div class="card-icon birth">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">出生年份</div>
              <div class="card-value">{{ selectedNode.birthYear }}</div>
            </div>
          </div>
          
          <div v-if="selectedNode.deathYear" class="detail-card">
            <div class="card-icon death">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">去世年份</div>
              <div class="card-value">{{ selectedNode.deathYear }}</div>
            </div>
          </div>
          
          <div v-if="selectedNode.lifespan" class="detail-card">
            <div class="card-icon age">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">享年</div>
              <div class="card-value">{{ selectedNode.lifespan }} 岁</div>
            </div>
          </div>

          <div v-if="selectedNode.occupation" class="detail-card">
            <div class="card-icon work">
              <el-icon><Briefcase /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">职业</div>
              <div class="card-value">{{ selectedNode.occupation }}</div>
            </div>
          </div>
        </div>

        
        <div v-if="selectedNode.birthplace" class="detail-section">
          <div class="detail-label">
            <el-icon><Location /></el-icon>
            籍贯
          </div>
          <div class="detail-value location-value">
            {{ selectedNode.birthplace }}
          </div>
        </div>

        
        <div v-if="selectedNode.spouse" class="detail-section">
          <div class="detail-label">
            <el-icon><UserFilled /></el-icon>
            配偶
          </div>
          <div class="detail-value">
            <div class="spouse-list">
              <el-tag 
                type="success" 
                v-for="s in (Array.isArray(selectedNode.spouse) ? selectedNode.spouse : [selectedNode.spouse])" 
                :key="s" 
                class="spouse-tag"
              >
                <el-icon><User /></el-icon>
                {{ s }}
              </el-tag>
            </div>
          </div>
        </div>

        
        <div v-if="selectedNode.children && selectedNode.children.length > 0" class="detail-section">
          <div class="detail-label">
            <el-icon><Connection /></el-icon>
            子女 ({{ selectedNode.children.length }})
          </div>
          <div class="detail-value">
            <div class="children-grid">
              <div 
                v-for="child in selectedNode.children" 
                :key="child.name"
                class="child-item"
                @click="showNodeDetails(child)"
              >
                <div class="child-avatar" :class="getGenderClass(child.name)">
                  <el-icon><User /></el-icon>
                </div>
                <div class="child-info">
                  <div class="child-name">{{ child.name }}</div>
                  <div class="child-relation">{{ child.relation || '子女' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        
        <div v-if="selectedNode.achievements" class="detail-section">
          <div class="detail-label">
            <el-icon><Trophy /></el-icon>
            主要成就
          </div>
          <div class="detail-value description-box">
            {{ selectedNode.achievements }}
          </div>
        </div>

        
        <div v-if="selectedNode.biography" class="detail-section">
          <div class="detail-label">
            <el-icon><Document /></el-icon>
            生平简介
          </div>
          <div class="detail-value description-box">
            {{ selectedNode.biography }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer-enhanced">
          <div class="navigation-section">
            <el-button 
              @click="navigateTree('prev')" 
              :disabled="!canNavigatePrev"
              class="nav-button"
            >
              <el-icon><ArrowLeft /></el-icon> 
              前一位成员
            </el-button>
            
            <div class="page-indicator">
              {{ currentNodeIndex + 1 }} / {{ flatNodeList.length }}
            </div>
            
            <el-button 
              @click="navigateTree('next')" 
              :disabled="!canNavigateNext"
              class="nav-button"
            >
              后一位成员
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          
          <el-button type="primary" @click="dialogVisible = false" class="close-button">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import * as echarts from "echarts/core";
import { TreeChart } from "echarts/charts";
import { TooltipComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
import { 
  ArrowLeft, 
  ArrowRight, 
  Location,
  Expand,
  Fold,
  User,
  Calendar,
  Clock,
  Timer,
  Briefcase,
  UserFilled,
  Connection,
  Trophy,
  Document,
  Close,
  DataBoard
} from '@element-plus/icons-vue';


echarts.use([TreeChart, TooltipComponent, CanvasRenderer]);

const props = defineProps({
  nodes: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref();
let chartInstance;

const dialogVisible = ref(false);
const selectedNode = ref(null);
const flatNodeList = ref([]);
const currentNodeIndex = ref(0);

const hasData = computed(() => Array.isArray(props.nodes) && props.nodes.length > 0);

const canNavigatePrev = computed(() => currentNodeIndex.value > 0);
const canNavigateNext = computed(() => currentNodeIndex.value < flatNodeList.value.length - 1);


const getGenderType = (name = "") => {
  if (/母|妻|女|妣|姑|姨|嫂|妹/.test(name)) return "danger";
  if (/父|公|子|兄|叔|伯|弟|祖|先/.test(name)) return "primary";
  return "warning";
};

const genderColor = (name = "") => {
  if (/母|妻|女|妣|姑|姨|嫂|妹/.test(name)) return "#e88fa3";
  if (/父|公|子|兄|叔|伯|弟|祖|先/.test(name)) return "#6b90d4";
  return "#c9b37c";
};


const getGenderClass = (name = "") => {
  if (/母|妻|女|妣|姑|姨|嫂|妹/.test(name)) return "female";
  if (/父|公|子|兄|叔|伯|弟|祖|先/.test(name)) return "male";
  return "other";
};

const getGenderText = (name = "") => {
  if (/母|妻|女|妣|姑|姨|嫂|妹/.test(name)) return "女性";
  if (/父|公|子|兄|叔|伯|弟|祖|先/.test(name)) return "男性";
  return "其他";
};


const flattenTree = (nodes, list = []) => {
  nodes.forEach(node => {
    list.push(node);
    if (node.children && node.children.length > 0) {
      flattenTree(node.children, list);
    }
  });
  return list;
};


const showNodeDetails = (node) => {
  selectedNode.value = node;
  currentNodeIndex.value = flatNodeList.value.findIndex(n => n.name === node.name);
  dialogVisible.value = true;
};


const navigateTree = (direction) => {
  if (direction === 'prev' && canNavigatePrev.value) {
    currentNodeIndex.value--;
  } else if (direction === 'next' && canNavigateNext.value) {
    currentNodeIndex.value++;
  }
  selectedNode.value = flatNodeList.value[currentNodeIndex.value];
};


const expandAll = () => {
  if (chartInstance) {
    chartInstance.dispatchAction({
      type: 'treeExpandAndCollapse',
      id: props.nodes[0]?.name,
      collapse: false
    });
  }
};

const collapseAll = () => {
  if (chartInstance) {
    chartInstance.dispatchAction({
      type: 'treeExpandAndCollapse',
      id: props.nodes[0]?.name,
      collapse: true
    });
  }
};

const enhanceNodes = (nodes) =>
  nodes.map((n) => ({
    ...n,
    itemStyle: {
      color: "#fffaf3",
      borderColor: genderColor(n.name),
      borderWidth: 2
    },
    label: {
      color: "#2f2b2a",
      fontSize: 13,
      backgroundColor: "rgba(255,255,255,0.8)",
      padding: [4, 8],
      borderRadius: 6
    },
    lineStyle: {
      color: "#c9b37c",
      width: 2.6,
      curveness: 0.25
    },
    children: n.children ? enhanceNodes(n.children) : []
  }));

const renderChart = () => {
  if (!hasData.value) {
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
    return;
  }
  if (!chartRef.value) return;
  
  
  flatNodeList.value = flattenTree(props.nodes);
  
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
    
   
    chartInstance.on('click', (params) => {
      if (params.componentType === 'series' && params.data) {
        showNodeDetails(params.data);
      }
    });
  }
  
  const data = enhanceNodes(props.nodes);
  chartInstance.setOption({
    tooltip: {
      trigger: "item",
      triggerOn: "mousemove",
      backgroundColor: "rgba(255,255,255,0.95)",
      borderColor: "#d1b17e",
      textStyle: { color: "#2f2b2a" },
      formatter: (params) => {
        const d = params.data;
        return `<strong>${d.name || ""}</strong>${d.relation ? "（" + d.relation + "）" : ""}<br/><span style="color: #d1b17e; font-size: 12px;">点击查看详情</span>`;
      }
    },
    series: [
      {
        type: "tree",
        data,
        top: "8%",
        left: "8%",
        bottom: "8%",
        right: "8%",
        orient: "TB",
        edgeShape: "curve",
        edgeForkPosition: "50%",
        symbol: "circle",
        symbolSize: 20,
        expandAndCollapse: true,
        initialTreeDepth: 5,
        animationDuration: 450,
        animationDurationUpdate: 650,
        emphasis: {
          focus: 'ancestor',
          itemStyle: {
            borderWidth: 3
          },
          label: {
            fontSize: 15,
            fontWeight: 600
          }
        }
      }
    ]
  });
};

onMounted(() => {
  renderChart();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  if (chartInstance) {
    chartInstance.dispose();
  }
});

watch(
  () => props.nodes,
  () => renderChart(),
  { deep: true }
);

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};
</script>

<style scoped>
.family-tree {
  width: 100%;
  height: 700px;
  position: relative;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.06);
}


.tree-header {
  padding: 24px 28px 16px;
  position: relative;
  z-index: 1;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.tree-header h3 {
  margin: 0;
  color: #2f2b2a;
  font-weight: 700;
  font-size: 22px;
  background: linear-gradient(135deg, #2f2b2a 0%, #8b7355 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.tree-header .subtitle {
  color: #8a8178;
  font-size: 14px;
  font-weight: 500;
}

.tree-controls {
  display: flex;
  gap: 8px;
}

.control-btn {
  border-radius: 16px;
  font-weight: 500;
}


.chart-enhanced-container {
  width: 100%;
  height: calc(100% - 80px);
  position: relative;
  padding: 0;
  overflow: hidden;
  background: 
    radial-gradient(circle at 20% 30%, rgba(220, 229, 243, 0.3), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(250, 225, 210, 0.25), transparent 50%);
}

.chart-enhanced {
  width: 100%;
  height: 100%;
  cursor: pointer;
  min-height: 500px;
}


.family-legend {
  position: absolute;
  bottom: 16px;
  left: 16px;
  z-index: 3;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(8px);
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-color.male {
  background: #6b90d4;
}

.legend-color.female {
  background: #e88fa3;
}

.legend-color.other {
  background: #c9b37c;
}


.empty-state {
  width: 100%;
  height: calc(100% - 80px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8a8178;
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.8);
}

.empty-icon {
  font-size: 64px;
  color: #d1d1d1;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-text {
  font-size: 18px;
  color: #8a8178;
  font-weight: 500;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #b8b0a7;
}


.family-dialog-enhanced {
  --el-dialog-bg-color: #fefefe;
}

.dialog-content-enhanced {
  padding: 16px 8px;
  max-height: 65vh;
  overflow-y: auto;
}


.member-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  border-left: 4px solid #a58938;
}

.member-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.member-avatar.male {
  background: linear-gradient(135deg, #6b90d4, #4a72c2);
}

.member-avatar.female {
  background: linear-gradient(135deg, #e88fa3, #d87093);
}

.member-avatar.other {
  background: linear-gradient(135deg, #c9b37c, #b89c5a);
}

.member-basic-info {
  flex: 1;
}

.member-name {
  font-size: 24px;
  font-weight: 700;
  color: #2f2b2a;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.gender-tag {
  font-size: 12px;
  padding: 2px 8px;
}

.member-relation {
  font-size: 16px;
  color: #8a8178;
  font-weight: 500;
}


.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.detail-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.detail-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.card-icon.birth {
  background: linear-gradient(135deg, #4CAF50, #45a049);
}

.card-icon.death {
  background: linear-gradient(135deg, #9E9E9E, #757575);
}

.card-icon.age {
  background: linear-gradient(135deg, #FF9800, #F57C00);
}

.card-icon.work {
  background: linear-gradient(135deg, #2196F3, #1976D2);
}

.card-content {
  flex: 1;
}

.card-label {
  font-size: 12px;
  color: #9b8b7a;
  font-weight: 600;
  margin-bottom: 6px;
}

.card-value {
  font-size: 16px;
  color: #2c2416;
  font-weight: 600;
}


.detail-section {
  margin-bottom: 24px;
}

.detail-label {
  font-weight: 600;
  color: #2f2b2a;
  margin-bottom: 12px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-value {
  color: #6a645f;
  font-size: 15px;
  line-height: 1.6;
}

.location-value {
  padding: 12px 16px;
  background: rgba(233, 236, 239, 0.6);
  border-radius: 8px;
  border-left: 4px solid #6b90d4;
}


.spouse-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spouse-tag {
  padding: 8px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.children-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.child-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
}

.child-item:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.child-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
  flex-shrink: 0;
}

.child-avatar.male {
  background: #6b90d4;
}

.child-avatar.female {
  background: #e88fa3;
}

.child-avatar.other {
  background: #c9b37c;
}

.child-info {
  flex: 1;
  min-width: 0;
}

.child-name {
  font-size: 14px;
  font-weight: 600;
  color: #2f2b2a;
  margin-bottom: 2px;
}

.child-relation {
  font-size: 12px;
  color: #8a8178;
}

.description-box {
  padding: 20px;
  background: linear-gradient(135deg, #fef9f0 0%, #fdf5e8 100%);
  border-radius: 12px;
  border-left: 4px solid #c9b37c;
  line-height: 1.7;
  font-size: 15px;
}


.dialog-footer-enhanced {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 0;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}

.navigation-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 2px solid #e8e6e4;
  background: white;
  border-radius: 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.nav-button:hover:not(:disabled) {
  background: #a58938;
  color: white;
  border-color: #a58938;
  transform: translateY(-1px);
}

.page-indicator {
  font-size: 14px;
  color: #8a8178;
  font-weight: 600;
  padding: 0 16px;
}

.close-button {
  padding: 10px 24px;
  border-radius: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}


@media (max-width: 768px) {
  .family-tree {
    height: 600px;
  }
  
  .tree-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .chart-enhanced-container {
    height: calc(100% - 120px);
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .children-grid {
    grid-template-columns: 1fr;
  }
  
  .dialog-footer-enhanced {
    flex-direction: column;
    gap: 16px;
  }
  
  .navigation-section {
    order: -1;
    width: 100%;
    justify-content: space-between;
  }
}


.chart {
  width: 100%;
  height: 100%;
}

.empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a8178;
  font-size: 14px;
}
</style>