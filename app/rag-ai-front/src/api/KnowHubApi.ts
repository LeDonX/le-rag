import { KnowApi } from "./common";
import { BASE_URL } from "@/http/config";
import axios from "axios";
import { DownloadFileDto,DeleteFileDto, QueryFileDto } from "./dto";
import service from "@/http";
import handleAuthError from "@/api/authUtils";

type Res = any;

const fileService = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "multipart/form-data",
  },
});


// 创建请求拦截器
fileService.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token"); 
    if (token !== null) {
      config.headers.Authorization = "Bearer " + token;
    }
    return config;
  },
  (error) => {
    console.log(error);
    return Promise.reject(error);
  }
);

// 创建响应拦截器
fileService.interceptors.response.use(
  (res: any) => { 
    
    return res;
  },
  (error) => { 
    // 检查是否是401错误
    if (error.response && error.response.status === 401) {
      handleAuthError();
    }
    
    console.log(error);
    return Promise.reject(error);
  }
);

// 上传知识库接口
export const uploadFileApi = async (filesList: File[]): Promise<Res> => {
  let formData = new FormData();
  filesList.map((e) => {
    formData.append("file", e);
  });

  return fileService.post(KnowApi.UploadFile, formData);
};

// 查询所有知识库接口
export const queryFileApi = async (params: QueryFileDto): Promise<Res> => {
  console.log("请求参数：", params);

  return service.get(KnowApi.QueryFile, {
    params,
  });
};

// 删除指定ID列表的知识库
export const deleteFileApi = async (params: DeleteFileDto): Promise<Res> => {
  return service.delete(KnowApi.DeleteFile, {
    params,
  });
};

// 下载指定ID列表的知识库
export const downloadFileApi = async (params: DownloadFileDto): Promise<Res> => {
  return service.get(KnowApi.DownloadFile, {
    params,
  });
};