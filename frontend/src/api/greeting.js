import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const greetingApi = {
  getGreeting: async (locale = 'zh-CN') => {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/greeting`, {
        headers: {
          'Accept-Language': locale
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching greeting:', error);
      return null;
    }
  }
};