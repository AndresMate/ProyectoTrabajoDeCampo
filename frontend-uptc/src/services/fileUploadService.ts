import api from "./api";

const fileUploadService = {
  uploadIdCard: async (file: File): Promise<string> => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await api.post('/files/upload/id-card', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      return response.data.fileUrl;
    } catch (error) {
      console.error('Error al subir archivo:', error);
      throw error;
    }
  }
};

export default fileUploadService;