import apiClient from './api';
import axios from 'axios';
import { Resume } from '../types/Resume';

export interface ResumeUploadResponse {
    resume: Resume;
    message: string;
    qualityScore?: number;
    qualitySuggestions?: string[];
}

export interface ResumeSearchParams {
    query?: string;
    status?: string;
    page?: number;
    size?: number;
}

class ResumeService {
    async uploadResume(file: File, onProgress?: (progress: number) => void): Promise<ResumeUploadResponse> {
        const formData = new FormData();
        formData.append('file', file);

        // Direct connection to Resume Service (bypass API Gateway for file uploads)
        const RESUME_SERVICE_URL = process.env.REACT_APP_RESUME_SERVICE_URL || 'http://localhost:8081/api/v1';
        const token = localStorage.getItem('authToken');

        const response = await axios.post<ResumeUploadResponse>(
            `${RESUME_SERVICE_URL}/resumes/upload`,
            formData,
            {
                headers: {
                    'Authorization': token ? `Bearer ${token}` : '',
                },
                onUploadProgress: (progressEvent) => {
                    if (onProgress && progressEvent.total) {
                        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                        onProgress(percentCompleted);
                    }
                },
            }
        );
        return response.data;
    }

    async getResume(id: string): Promise<Resume> {
        const response = await apiClient.get<Resume>(`/resumes/${id}`);
        return response.data;
    }

    async getMyResumes(): Promise<Resume[]> {
        const response = await apiClient.get<Resume[]>('/resumes/active');
        return response.data;
    }

    async searchResumes(params: ResumeSearchParams): Promise<{ resumes: Resume[], total: number }> {
        const response = await apiClient.get('/resumes/search', { params });
        return response.data;
    }

    async updateResumeStatus(id: string, status: string): Promise<Resume> {
        const response = await apiClient.patch<Resume>(`/resumes/${id}/status`, { status });
        return response.data;
    }

    async deleteResume(id: string): Promise<void> {
        await apiClient.delete(`/resumes/${id}`);
    }
}

export default new ResumeService();
