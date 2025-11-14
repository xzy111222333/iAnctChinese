import { defineStore } from "pinia";
import { fetchTexts, fetchTextById, uploadText, updateTextCategory, exportText } from "@/api/texts";
import { fetchEntities, fetchRelations, createEntity, createRelation } from "@/api/annotations";
import { classifyText, fetchInsights, autoAnnotate } from "@/api/analysis";
import { fetchSections, autoSegment, updateSection as updateSectionApi } from "@/api/sections";
import { fetchNavigationTree } from "@/api/navigation";
import { searchTexts } from "@/api/search";

export const useTextStore = defineStore("textStore", {
  state: () => ({
    texts: [],
    selectedTextId: null,
    selectedText: null,
    entities: [],
    relations: [],
    insights: null,
    classification: null,
    sections: [],
    navigationTree: null,
    searchResults: [],
    searchVersion: 0,
    searchLoading: false,
    exporting: false,
    loading: false,
    saving: false,
    filters: {
      entityCategories: [],
      relationTypes: [],
      highlightOnly: false
    }
  }),
  getters: {
    entityOptions(state) {
      const options = new Set(state.entities.map((entity) => entity.category));
      return Array.from(options);
    },
    relationOptions(state) {
      const options = new Set(state.relations.map((relation) => relation.relationType));
      return Array.from(options);
    }
  },
  actions: {
    async initDashboard() {
      await this.loadTexts();
      await this.loadNavigationTree();
      if (this.texts.length > 0) {
        await this.selectText(this.texts[0].id);
      }
    },
    async loadTexts(category) {
      this.loading = true;
      try {
        const { data } = await fetchTexts(category);
        this.texts = data;
      } finally {
        this.loading = false;
      }
    },
    async selectText(id) {
      if (!id) {
        return;
      }
      this.selectedTextId = id;
      this.loading = true;
      try {
        const [
          { data: text },
          { data: entityData },
          { data: relationData },
          { data: insightsData },
          { data: sectionsData }
        ] = await Promise.all([
          fetchTextById(id),
          fetchEntities(id),
          fetchRelations(id),
          fetchInsights(id),
          fetchSections(id)
        ]);
        this.selectedText = text;
        this.entities = entityData;
        this.relations = relationData;
        this.insights = insightsData;
        this.sections = sectionsData;
        this.filters.entityCategories = [...this.entityOptions];
        this.filters.relationTypes = [...this.relationOptions];
      } finally {
        this.loading = false;
      }
    },
    async uploadNewText(payload) {
      this.saving = true;
      try {
        const { data } = await uploadText(payload);
        this.texts.unshift(data);
        await this.selectText(data.id);
        await this.loadNavigationTree();
      } finally {
        this.saving = false;
      }
    },
    async createEntityAnnotation(payload) {
      await createEntity(payload);
      await this.selectText(payload.textId);
    },
    async createRelationAnnotation(payload) {
      await createRelation(payload);
      await this.selectText(payload.textId);
    },
    async classifySelectedText() {
      if (!this.selectedTextId) {
        return;
      }
      const { data } = await classifyText(this.selectedTextId);
      this.classification = data;
      await this.selectText(this.selectedTextId);
    },
    async triggerAutoAnnotation() {
      if (!this.selectedTextId) {
        return;
      }
      await autoAnnotate(this.selectedTextId);
      await this.selectText(this.selectedTextId);
    },
    async updateSelectedCategory(category) {
      if (!this.selectedTextId) {
        return;
      }
      await updateTextCategory(this.selectedTextId, category);
      if (this.selectedText) {
        this.selectedText.category = category;
      }
      const textIndex = this.texts.findIndex((item) => item.id === this.selectedTextId);
      if (textIndex >= 0) {
        this.texts[textIndex].category = category;
      }
    },
    toggleHighlightOnly() {
      this.filters.highlightOnly = !this.filters.highlightOnly;
    },
    setHighlightOnly(value) {
      this.filters.highlightOnly = value;
    },
    setEntityFilters(values) {
      this.filters.entityCategories = values;
    },
    setRelationFilters(values) {
      this.filters.relationTypes = values;
    },
    async autoSegmentSections() {
      if (!this.selectedTextId) {
        return;
      }
      const { data } = await autoSegment(this.selectedTextId);
      this.sections = data;
    },
    async updateSection(sectionId, payload) {
      const { data } = await updateSectionApi(sectionId, payload);
      const index = this.sections.findIndex((section) => section.id === sectionId);
      if (index >= 0) {
        this.sections[index] = data;
      }
    },
    async loadNavigationTree() {
      const { data } = await fetchNavigationTree();
      this.navigationTree = data;
    },
    async performSearch(keyword) {
      if (!keyword) {
        this.searchResults = [];
        return;
      }
      this.searchLoading = true;
      try {
        const { data } = await searchTexts(keyword);
        this.searchResults = data;
        this.searchVersion += 1;
      } finally {
        this.searchLoading = false;
      }
    },
    async exportSelectedText() {
      if (!this.selectedTextId) {
        return;
      }
      this.exporting = true;
      try {
        const response = await exportText(this.selectedTextId);
        const blob = new Blob([response.data], { type: "application/json" });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = `text-${this.selectedTextId}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } finally {
        this.exporting = false;
      }
    }
  }
});
