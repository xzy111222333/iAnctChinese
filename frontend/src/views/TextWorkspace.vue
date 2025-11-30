<template>
  <div class="workspace-layout">
    <WorkspaceHeader 
      v-model:currentTab="currentTab" 
    />
    
    <div class="workspace-body">
      <!-- Left Sidebar -->
      <EntityToolbox 
        v-if="['entity', 'relation'].includes(currentTab)"
        :entities="store.entities || []"
        @selectCategory="handleCategorySelect"
      />
      
      <!-- Main Content Area -->
      <main class="main-area">
        <!-- Entity Annotation Mode -->
        <AnnotatorEditor 
          v-if="currentTab === 'entity'"
          :content="store.selectedText?.content"
          :entities="store.entities || []"
          @removeEntity="handleRemoveEntity"
          @addEntity="handleAddEntity"
          @runAnalysis="handleRunAnalysis"
        />

        <!-- Structure Mode (Segments) -->
        <div v-else-if="currentTab === 'structure'" class="structure-view">
           <div class="view-header">
             <h3>句读与结构分段</h3>
             <el-button type="primary" size="small" @click="handleAutoSegment">自动推荐句读</el-button>
           </div>
           <div class="segments-container">
              <div v-if="!store.sections?.length" class="empty-placeholder">暂无分段数据</div>
              <div v-for="section in store.sections" :key="section.id" class="segment-row">
                 <div class="seg-orig">{{ section.originalText }}</div>
                 <div class="seg-punc">
                    <el-input 
                      v-model="section.punctuatedText" 
                      type="textarea" 
                      :rows="2" 
                      placeholder="句读文本"
                      @blur="handleUpdateSection(section)"
                    />
                 </div>
              </div>
           </div>
        </div>

        <!-- Graph Mode -->
        <div v-else-if="currentTab === 'graph'" class="graph-view">
           <GraphView 
             :entities="store.entities || []" 
             :relations="store.relations || []"
             style="width: 100%; height: 100%"
           />
        </div>
        
         <!-- Placeholder for other modes -->
        <div v-else class="empty-placeholder">
          Feature under construction: {{ currentTab }}
        </div>
      </main>

      <!-- Right Sidebar -->
      <InfoPanel 
        :text="store.selectedText"
        :entitiesCount="store.entities?.length || 0"
        :relationsCount="store.relations?.length || 0"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useTextStore } from "@/store/textStore";
import { ElMessage } from "element-plus";
import WorkspaceHeader from "@/components/workspace/WorkspaceHeader.vue";
import EntityToolbox from "@/components/workspace/EntityToolbox.vue";
import AnnotatorEditor from "@/components/workspace/AnnotatorEditor.vue";
import InfoPanel from "@/components/workspace/InfoPanel.vue";
import GraphView from "@/components/visualizations/GraphView.vue";

const store = useTextStore();
const currentTab = ref("entity");

const handleCategorySelect = (category) => {
  // Logic to scroll to first entity of this category?
  // Or filter editor?
  // For now just log
  console.log("Selected category:", category);
};

const handleRemoveEntity = async (id) => {
  try {
    // Assuming store has a delete action, if not we mock or skip
    // store.deleteEntity(id); 
    // Checking store capabilities from memory/previous file... 
    // It seems previous file didn't use delete.
    // Let's assume we can just refresh or ignore for now if API not ready.
    ElMessage.info("删除功能待后端接口对接 (" + id + ")");
  } catch (e) {
    console.error(e);
  }
};

const handleAddEntity = (data) => {
  // logic to add entity
};

const handleRunAnalysis = async () => {
  try {
    ElMessage.info("正在启动模型分析...");
    // Use a default model for now since we hid the selector. 
    // In a real app we'd show a modal.
    const defaultModel = "deepseek-ai/DeepSeek-V3"; 
    await store.runFullAnalysis(defaultModel);
    ElMessage.success("模型分析完成");
  } catch (error) {
    ElMessage.error("分析失败");
  }
};

const handleAutoSegment = async () => {
  await store.autoSegmentSections();
  ElMessage.success("自动句读完成");
};

const handleUpdateSection = async (section) => {
  await store.updateSection(section.id, {
    punctuatedText: section.punctuatedText
  });
  ElMessage.success("已保存");
};

</script>

<style scoped>
.workspace-layout {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #FAFAFB;
  display: flex;
  flex-direction: column;
  z-index: 2000; /* Above everything */
}

.workspace-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.structure-view, .graph-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow-y: auto;
  background: #fff;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.segments-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 40px;
}

.segment-row {
  border: 1px solid #EDEEF0;
  border-radius: 8px;
  padding: 16px;
  background: #FAFAFB;
}

.seg-orig {
  font-size: 16px;
  color: #1F2328;
  margin-bottom: 12px;
  font-family: "PingFang SC";
  line-height: 1.6;
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8B95A7;
  font-size: 16px;
}
</style>
