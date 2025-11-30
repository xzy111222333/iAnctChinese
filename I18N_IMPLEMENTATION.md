# 国际化 (i18n) 实现说明

## 功能概述

本项目已成功实现了中英文国际化支持，包括前端和后端的完整国际化方案。

## 后端国际化实现

### 1. 消息资源文件

- `backend/src/main/resources/messages.properties` - 英文消息
- `backend/src/main/resources/messages_zh_CN.properties` - 中文消息

### 2. 配置类

- `I18nConfig.java` - Spring Boot国际化配置
  - 配置了MessageSource
  - 设置了LocaleResolver，支持Accept-Language头
  - 默认语言为中文，支持中英文切换

### 3. API端点

- `GreetingController.java` - 问候语API
  - GET `/api/greeting` - 返回当前语言的问候语信息
  - 支持通过Accept-Language头指定语言

## 前端国际化实现

### 1. 依赖安装

- 安装了 `vue-i18n@9` 用于前端国际化

### 2. 语言包文件

- `frontend/src/locales/en.json` - 英文语言包
- `frontend/src/locales/zh-CN.json` - 中文语言包

### 3. 配置文件

- `frontend/src/i18n.js` - Vue i18n配置
  - 默认语言为中文(zh-CN)
  - 回退语言为英文(en)

### 4. 组件更新

- `AppShell.vue` - 更新为使用国际化文本
  - 添加了语言切换下拉菜单
  - 所有UI文本都使用 `$t()` 函数进行国际化
  - 支持语言偏好持久化到localStorage

- `GreetingDisplay.vue` - 新增组件
  - 调用后端问候语API
  - 展示后端国际化功能
  - 监听语言切换并自动更新

- `DashboardView.vue` - 集成问候语显示组件

## API集成

### 前端API服务

- `frontend/src/api/greeting.js` - 问候语API服务
  - 支持Accept-Language头
  - 错误处理机制

## 使用方法

### 前端语言切换

1. 在页面顶部的语言切换下拉菜单中选择语言
2. 选择"中文"或"English"
3. 语言偏好会自动保存到localStorage
4. 页面会立即切换到选择的语言

### 后端API调用

前端会自动根据当前选择的语言调用后端API：
```javascript
// 示例：获取中文问候语
GET /api/greeting
Accept-Language: zh-CN

// 示例：获取英文问候语  
GET /api/greeting
Accept-Language: en
```

## 支持的语言

- 中文 (zh-CN) - 简体中文
- 英文 (en) - English

## 扩展指南

### 添加新语言

#### 后端

1. 创建新的消息资源文件，如 `messages_ja.properties` (日文)
2. 在 `I18nConfig.java` 中添加新的Locale支持

#### 前端

1. 创建新的语言包文件，如 `frontend/src/locales/ja.json`
2. 在 `i18n.js` 中添加新的语言包
3. 在 `AppShell.vue` 的语言选择器中添加新选项

### 添加新的国际化文本

1. 在后端的 `messages*.properties` 文件中添加新的键值对
2. 在前端的语言包JSON文件中添加对应的翻译
3. 在Vue组件中使用 `$t('key')` 调用

## 技术特点

- **前后端分离的国际化**: 前端和后端都有自己的国际化方案
- **语言偏好持久化**: 前端语言选择会保存到localStorage
- **自动语言检测**: 后端支持Accept-Language头自动检测语言
- **无缝切换**: 语言切换时页面内容会立即更新
- **API集成**: 前端会根据当前语言调用对应的后端API

## 测试验证

1. 启动后端服务 (Spring Boot)
2. 启动前端服务 (Vue + Vite)
3. 访问应用，查看默认中文界面
4. 切换到英文，验证界面文本切换
5. 检查问候语显示组件是否调用后端API并显示正确语言的内容

这个实现为古籍智能标注平台提供了完整的国际化基础，后续可以轻松扩展到更多语言支持。