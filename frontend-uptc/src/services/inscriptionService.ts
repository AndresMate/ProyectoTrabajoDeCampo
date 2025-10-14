import api from "./api";

export const getInscriptions = async () => {
  const response = await api.get("/inscriptions");
  return response.data;
};

export const getInscriptionById = async (id: number) => {
  const response = await api.get(`/inscriptions/${id}`);
  return response.data;
};

export const createInscription = async (data: any) => {
  const response = await api.post("/inscriptions", data);
  return response.data;
};

export const updateInscription = async (id: number, data: any) => {
  const response = await api.put(`/inscriptions/${id}`, data);
  return response.data;
};

export const deleteInscription = async (id: number) => {
  await api.delete(`/inscriptions/${id}`);
};
