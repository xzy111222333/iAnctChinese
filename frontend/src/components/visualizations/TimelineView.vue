<template>
  <div class="timeline-container">
    <div class="timeline-bg"></div>
    <div class="timeline-axis">
      <div class="axis-line"></div>
      <div
        v-for="(milestone, index) in milestones"
        :key="milestone.title + index"
        class="timeline-card"
      >
        <div class="node">
          <span>{{ milestone.dateLabel }}</span>
        </div>
        <div class="card-content">
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
</script>

<style scoped>
.timeline-container {
  position: relative;
  overflow-x: auto;
  padding: 24px 12px 12px;
  min-height: 320px;
}

.timeline-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(243, 229, 204, 0.6), rgba(202, 220, 236, 0.6));
  border-radius: 24px;
  filter: blur(20px);
  z-index: 0;
}

.timeline-axis {
  position: relative;
  display: flex;
  gap: 32px;
  padding: 12px 12px 24px;
  z-index: 1;
  min-width: 640px;
}

.axis-line {
  position: absolute;
  top: 72px;
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
  padding-top: 64px;
}

.timeline-card::after {
  content: "";
  position: absolute;
  top: 75px;
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
  background: white;
  border-radius: 50%;
  border: 3px solid #f3b176;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #b9803b;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
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
</style>
