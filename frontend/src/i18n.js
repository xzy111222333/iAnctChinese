import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import zhCN from './locales/zh-CN.json'

const messages = {
  en,
  'zh-CN': zhCN
}

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN', // default locale
  fallbackLocale: 'en',
  messages
})

export default i18n