<template>
  <div ref="chartRef" class="family-tree"></div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from "vue";
import * as echarts from "echarts/core";
import { TreeChart } from "echarts/charts";
import { TooltipComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

echarts.use([TreeChart, TooltipComponent, CanvasRenderer]);

const props = defineProps({
  nodes: {
    type: Array,
    default: () => []
  }
});

const chartRef = ref();
let chartInstance;

const renderChart = () => {
  if (!chartRef.value) {
    return;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  const data = props.nodes.length ? props.nodes : [{ name: "暂无数据", children: [] }];
  chartInstance.setOption({
    tooltip: {
      trigger: "item",
      triggerOn: "mousemove",
      formatter: (params) => `${params.data.name} (${params.data.relation || ""})`
    },
    series: [
      {
        type: "tree",
        data,
        top: "5%",
        left: "10%",
        bottom: "5%",
        right: "20%",
        symbol: "circle",
        symbolSize: 14,
        label: {
          position: "left",
          verticalAlign: "middle",
          align: "right",
          fontSize: 12
        },
        leaves: {
          label: {
            position: "right",
            verticalAlign: "middle",
            align: "left"
          }
        },
        expandAndCollapse: true,
        initialTreeDepth: 3,
        animationDuration: 550,
        animationDurationUpdate: 750
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
  () => {
    renderChart();
  },
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
  height: 480px;
}
</style>
