<template>
  <div class="shell">
    <header class="global-bar">
      <div class="title">
        <span>{{ $t('greeting.welcome') }}</span>
        <small>{{ $t('greeting.project') }} | {{ $t('greeting.type') }} | {{ $t('greeting.time') }} | {{ $t('greeting.scope') }} | {{ $t('greeting.searchNav') }} | {{ $t('greeting.exportNav') }}</small>
      </div>
      <div class="actions">
        <el-select v-model="currentLocale" @change="changeLocale" size="large" style="width: 120px; margin-right: 12px;">
          <el-option label="中文" value="zh-CN" />
          <el-option label="English" value="en" />
        </el-select>
        <el-input
          v-model="keywords"
          :placeholder="$t('greeting.searchPlaceholder')"
          clearable
          prefix-icon="Search"
          size="large"
        />
        <el-button type="primary" @click="emitSearch" size="large">{{ $t('greeting.search') }}</el-button>
        <el-button :loading="props.exporting" :disabled="!props.canExport" @click="$emit('export')">
          {{ $t('greeting.export') }}
        </el-button>
      </div>
    </header>
    <main class="content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const { locale } = useI18n();

const props = defineProps({
  exporting: {
    type: Boolean,
    default: false
  },
  canExport: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(["search", "export"]);
const keywords = ref("");
const currentLocale = ref(locale.value);

const changeLocale = (newLocale) => {
  locale.value = newLocale;
  localStorage.setItem('locale', newLocale);
};

const emitSearch = () => {
  emit("search", keywords.value);
};

// Initialize locale from localStorage
const savedLocale = localStorage.getItem('locale');
if (savedLocale) {
  locale.value = savedLocale;
  currentLocale.value = savedLocale;
}
</script>

<style scoped>
.shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.global-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
}

.title {
  display: flex;
  flex-direction: column;
  font-size: 20px;
  font-weight: 600;
}

.title small {
  font-size: 12px;
  color: var(--muted);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  flex: 1;
  padding: 24px;
}
</style>
