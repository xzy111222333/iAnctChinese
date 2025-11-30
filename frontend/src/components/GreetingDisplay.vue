<template>
  <div class="greeting-display" v-if="greetingData">
    <h3>{{ greetingData.welcome }}</h3>
    <p>{{ greetingData.platform }}</p>
    <p>{{ greetingData.description }}</p>
    <small>Locale: {{ greetingData.locale }}</small>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { greetingApi } from '@/api/greeting';

const { locale } = useI18n();
const greetingData = ref(null);

const fetchGreeting = async () => {
  const data = await greetingApi.getGreeting(locale.value);
  if (data) {
    greetingData.value = data;
  }
};

// Watch for locale changes
watch(locale, fetchGreeting);

// Fetch on mount
onMounted(fetchGreeting);
</script>

<style scoped>
.greeting-display {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.greeting-display h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.greeting-display p {
  margin: 4px 0;
  color: #666;
}

.greeting-display small {
  color: #999;
  font-style: italic;
}
</style>