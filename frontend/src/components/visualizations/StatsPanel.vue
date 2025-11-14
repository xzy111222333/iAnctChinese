<template>
  <section class="panel stats-panel">
    <h3 class="section-title">词频词云</h3>
    <div class="word-cloud">
      <span
        v-for="word in words"
        :key="word.label"
        class="word"
        :style="{ fontSize: `${word.weight * 8 + 12}px`, color: pickColor(word.weight) }"
      >
        {{ word.label }}
      </span>
    </div>
    <el-divider />
    <h3 class="section-title">统计信息</h3>
    <ul>
      <li>实体数：{{ stats?.entityCount ?? 0 }}</li>
      <li>关系数：{{ stats?.relationCount ?? 0 }}</li>
      <li>句读完成度：{{ Math.round(((stats?.punctuationProgress) ?? 0) * 100) }}%</li>
    </ul>
    <p class="analysis">{{ analysisSummary }}</p>
  </section>
</template>

<script setup>
const props = defineProps({
  words: {
    type: Array,
    default: () => []
  },
  stats: {
    type: Object,
    default: () => ({
      entityCount: 0,
      relationCount: 0,
      punctuationProgress: 0
    })
  },
  analysisSummary: {
    type: String,
    default: ""
  }
});

const pickColor = (weight) => {
  if (weight > 0.8) {
    return "#c05621";
  }
  if (weight > 0.5) {
    return "#8f6f35";
  }
  return "#5f6f7c";
};
</script>

<style scoped>
.word-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 120px;
}

.word {
  font-weight: 600;
}

ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
  color: var(--muted);
}
</style>
.analysis {
  margin-top: 12px;
  color: var(--muted);
  line-height: 1.6;
}
