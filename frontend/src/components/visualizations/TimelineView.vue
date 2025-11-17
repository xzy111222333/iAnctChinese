<template>
  <div class="timeline-container">
    <div class="timeline-bg"></div>
    <div class="timeline-header">
      <h3>发展历程</h3>
      <span class="sub">生平节点 / 行军路标 / 事件脉络</span>
    </div>
    <div class="timeline-axis">
      <div class="axis-line"></div>
      <div
        v-for="(milestone, index) in milestones"
        :key="milestone.title + index"
        class="timeline-card"
      >
        <div class="node" :style="{ borderColor: pickColor(index), background: pointBg(index) }">
          <span>{{ milestone.dateLabel }}</span>
        </div>
        <div class="card-content" :style="{ borderColor: pickColor(index) }">
          <div class="card-head">
            <span class="date">{{ milestone.dateLabel || "未注明" }}</span>
            <span class="badge" :style="{ background: pickColor(index) }">{{ milestone.significance || 1 }}</span>
          </div>
          <h4>{{ milestone.title }}</h4>
          <p>{{ milestone.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  milestones: {
    type: Array,
    default: () => []
  }
});

const pickColor = (idx) => {
  const colors = ["#d16a5d", "#3a7a87", "#a58938", "#6b6fb5", "#c06d9d"];
  return colors[idx % colors.length];
};

const pointBg = (idx) => {
  const color = pickColor(idx);
  return `radial-gradient(circle at center, ${color} 0%, ${color} 35%, transparent 60%)`;
};
</script>

<style scoped>
.timeline-container {
  position: relative;
  overflow-x: auto;
  padding: 28px 16px 16px;
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

.timeline-axis {
  position: relative;
  display: flex;
  gap: 32px;
  padding: 16px 12px 24px;
  z-index: 1;
  min-width: 640px;
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
  min-width: 180px;
  position: relative;
  padding-top: 84px;
}

.timeline-card::after {
  content: "";
  position: absolute;
  top: 95px;
  left: 50%;
  width: 2px;
  height: 40px;
  background: rgba(0, 0, 0, 0.1);
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
  font-size: 13px;
  color: #2f2b2a;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(8px);
}

.card-content {
  background: white;
  border-radius: 16px;
  padding: 12px 16px;
  box-shadow: 0 12px 26px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.card-content h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #2f2b2a;
}

.card-content p {
  margin: 0;
  font-size: 13px;
  color: #6a645f;
  line-height: 1.5;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #8a8178;
}

.date {
  font-weight: 700;
}

.badge {
  color: #fff;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
}
</style>
