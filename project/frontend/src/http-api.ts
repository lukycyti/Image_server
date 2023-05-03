import axios, { AxiosResponse, AxiosError } from 'axios';
import { ImageType } from '@/image';

const instance = axios.create({
  baseURL: "/",
  timeout: 15000,
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
  post: (url: string, body: {}, config: {}) => instance.post(url, body, config).then(responseBody),
  put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};


export const api = {
  getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
  getImageAfterAlgorithm: (id: number, param: any): Promise<Blob> => requests.get(`/images/${id}/`, { responseType: "blob", params: param}),
  getImageFromData: (form: FormData):  Promise<Blob> => requests.post(`images/fromdata`, form, { headers: { "Content-Type": "multipart/form-data" }, responseType: "blob"}),
  getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob"}),
  getImageName: (id: number): Promise<String> => requests.get(`images/${id}`, {}),
  createImage: (form: FormData): Promise<ImageType> => requests.post('images', form, { headers: { "Content-Type": "multipart/form-data" }}),
  deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
};

