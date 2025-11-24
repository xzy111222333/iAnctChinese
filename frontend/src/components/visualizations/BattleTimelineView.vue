<template>
  <div class="battle-timeline">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="!hasData" class="empty">暂无战事数据</div>
    <div v-else>
      <div class="battle-header">
        <h3>战事时间线</h3>
        <span class="sub">点击查看战事详情</span>
      </div>
      <div ref="chartRef" class="chart"></div>
    </div>
    <div class="battle-overlay"></div>

   
    <el-dialog
      v-model="dialogVisible"
      :title="selectedBattle?.phase"
      width="600px"
      :close-on-click-modal="true"
      class="battle-dialog"
    >
      <div v-if="selectedBattle" class="dialog-content">
        <div class="detail-section">
          <div class="detail-label">战事阶段</div>
          <div class="detail-value">{{ selectedBattle.phase || '未注明' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">战事强度</div>
          <div class="detail-value">
            <el-rate 
              v-model="selectedBattle.intensity" 
              disabled 
              show-score 
              :max="10"
              text-color="#ff9900"
            />
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">对手</div>
          <div class="detail-value">
            <el-tag type="danger">{{ selectedBattle.opponent || '未知' }}</el-tag>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">战事描述</div>
          <div class="detail-value description">{{ selectedBattle.description }}</div>
        </div>
        <div v-if="selectedBattle.location" class="detail-section">
          <div class="detail-label">战场位置</div>
          <div class="detail-value">
            <el-icon><Location /></el-icon>
            {{ selectedBattle.location }}
          </div>
        </div>
        <div v-if="selectedBattle.casualties" class="detail-section">
          <div class="detail-label">伤亡情况</div>
          <div class="detail-value">{{ selectedBattle.casualties }}</div>
        </div>
        <div v-if="selectedBattle.outcome" class="detail-section">
          <div class="detail-label">战果</div>
          <div class="detail-value outcome">
            <el-tag :type="selectedBattle.outcome === '胜' ? 'success' : 'danger'">
              {{ selectedBattle.outcome }}
            </el-tag>
          </div>
        </div>
        <div v-if="selectedBattle.significance" class="detail-section">
          <div class="detail-label">历史意义</div>
          <div class="detail-value description">{{ selectedBattle.significance }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="prevBattle" :disabled="currentIndex === 0">
          <el-icon><ArrowLeft /></el-icon> 上一场
        </el-button>
        <el-button @click="nextBattle" :disabled="currentIndex === events.length - 1">
          下一场 <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import * as echarts from "echarts/core";
import { LineChart, PictorialBarChart, BarChart } from "echarts/charts";
import { GridComponent, TooltipComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
import { ArrowLeft, ArrowRight, Location } from '@element-plus/icons-vue';


const CHART_COLORS = {
  primary: "#f6c189",
  secondary: "#e77761",
  accent: "#d16a5d",
  line: "#f59a5f",
  background: {
    start: "rgba(255, 244, 229, 0.8)",
    end: "rgba(232, 240, 247, 0.9)"
  }
};

const CHART_CONFIG = {
  grid: {
    left: "5%",
    right: "5%",
    bottom: "10%",
    top: "10%",
    containLabel: true
  },
  xAxis: {
    type: "category",
    axisLine: { lineStyle: { color: "#c5b09b" } },
    axisLabel: { 
      color: "#5d554b",
      rotate: 30,
      interval: 0
    }
  },
  yAxis: {
    type: "value",
    min: 0,
    max: 10,
    name: "战事强度",
    nameTextStyle: { color: "#5d554b", fontWeight: 600 },
    axisLine: { show: false },
    axisLabel: { color: "#968d83" },
    splitLine: { lineStyle: { color: "rgba(0,0,0,0.08)" } }
  }
};


echarts.use([LineChart, PictorialBarChart, BarChart, GridComponent, TooltipComponent, CanvasRenderer]);

const props = defineProps({
  events: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref();
let chartInstance = null;
let resizeTimer = null;

const dialogVisible = ref(false);
const selectedBattle = ref(null);
const currentIndex = ref(0);
const loading = ref(false);

const hasData = computed(() => Array.isArray(props.events) && props.events.length > 0);


const debounce = (func, delay) => {
  return function(...args) {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(() => func.apply(this, args), delay);
  };
};


const showBattleDetails = (dataIndex) => {
  if (dataIndex >= 0 && dataIndex < props.events.length) {
    selectedBattle.value = props.events[dataIndex];
    currentIndex.value = dataIndex;
    dialogVisible.value = true;
  }
};


const prevBattle = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    selectedBattle.value = props.events[currentIndex.value];
  }
};


const nextBattle = () => {
  if (currentIndex.value < props.events.length - 1) {
    currentIndex.value++;
    selectedBattle.value = props.events[currentIndex.value];
  }
};


const createSeries = (intensity) => {
  return [
    {
      type: "pictorialBar",
      symbolSize: [18, 18],
      symbolOffset: [0, -8],
      z: 12,
      data: intensity.map((value) => ({
        value,
        symbol: "circle",
        itemStyle: { 
          color: CHART_COLORS.primary, 
          borderColor: CHART_COLORS.accent, 
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(209, 106, 93, 0.3)'
        }
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(209, 106, 93, 0.6)',
          borderWidth: 3
        }
      }
    },
    {
      type: "bar",
      data: intensity,
      barWidth: 32,
      itemStyle: {
        color: {
          type: "linear",
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: CHART_COLORS.primary },
            { offset: 1, color: CHART_COLORS.secondary }
          ]
        },
        borderRadius: [12, 12, 0, 0]
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(231, 119, 97, 0.5)'
        }
      }
    },
    {
      type: "line",
      data: intensity,
      smooth: true,
      symbol: "circle",
      symbolSize: 10,
      lineStyle: {
        color: CHART_COLORS.line,
        width: 3
      },
      areaStyle: {
        color: "rgba(245, 154, 95, 0.22)"
      },
      emphasis: {
        focus: 'series',
        lineStyle: {
          width: 4
        },
        itemStyle: {
          shadowBlur: 15,
          shadowColor: 'rgba(245, 154, 95, 0.6)',
          borderWidth: 3
        }
      }
    }
  ];
};


const formatTooltip = (params) => {
  const { name, dataIndex } = params[0];
  const event = props.events[dataIndex] || {};
  return `<strong>${name}</strong><br/>${event.description || ""}<br/>对手：${event.opponent || "未知"}<br/><span style="color: ${CHART_COLORS.accent}; font-size: 12px;">点击查看详情</span>`;
};


const renderChart = () => {
  try {
    if (!hasData.value) {
      if (chartInstance) {
        chartInstance.dispose();
        chartInstance = null;
      }
      return;
    }
    
    if (!chartRef.value) {
      return;
    }
    
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value);
      
    
      chartInstance.on('click', (params) => {
        if (params.componentType === 'series') {
          showBattleDetails(params.dataIndex);
        }
      });
    }
    
    const categories = props.events.map((event) => event?.phase || "");
    const intensity = props.events.map((event) => event?.intensity || 0);

    chartInstance.setOption({
      tooltip: {
        trigger: "axis",
        borderColor: CHART_COLORS.accent,
        backgroundColor: "rgba(255,255,255,0.95)",
        textStyle: { color: "#2f2b2a" },
        formatter: formatTooltip
      },
      ...CHART_CONFIG,
      xAxis: {
        ...CHART_CONFIG.xAxis,
        data: categories
      },
      series: createSeries(intensity)
    });

 
    chartInstance.getZr().setCursorStyle('pointer');
  } catch (err) {
    console.warn("battle chart render error", err);
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }
  }
};


const handleResize = debounce(() => {
  if (chartInstance) {
    chartInstance.resize();
  }
}, 200);

onMounted(() => {
  loading.value = true;
 
  setTimeout(() => {
    renderChart();
    loading.value = false;
  }, 300);
  
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});

watch(
  () => props.events,
  () => {
    if (!loading.value) {
      renderChart();
    }
  },
  { deep: true }
);
</script>

<style scoped>
.battle-timeline {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 24px;
  overflow: hidden;
  background: radial-gradient(circle at top left, v-bind('CHART_COLORS.background.start'), v-bind('CHART_COLORS.background.end'));
  padding: 16px;
}

.battle-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding-left: 6px;
  margin-bottom: 12px;
}

.battle-header h3 {
  margin: 0;
  color: #2f2b2a;
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 18px;
}

.battle-header .sub {
  color: #8a8178;
  font-size: 13px;
}

.chart {
  width: 100%;
  height: calc(100% - 50px);
  cursor: pointer;
}

.battle-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: url("data:image/svg+xml,%3Csvg width='200' height='200' viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Ccircle cx='100' cy='100' r='98' fill='none' stroke='%23f4dbc0' stroke-width='0.4'/%3E%3C/svg%3E")
    repeat;
  opacity: 0.35;
}

.empty, .loading {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a8178;
  font-size: 14px;
}

.loading {
  flex-direction: column;
  gap: 12px;
}

.loading::after {
  content: "";
  width: 24px;
  height: 24px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #d16a5d;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}


.battle-dialog .dialog-content {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 20px;
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
  background: #fff5f0;
  border-radius: 8px;
  border-left: 4px solid #d16a5d;
}

.detail-value.outcome {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-value .el-icon {
  margin-right: 4px;
  color: #d16a5d;
}
</style>