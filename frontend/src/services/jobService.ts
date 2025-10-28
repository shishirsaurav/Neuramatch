import apiClient from './api';
import { Job } from '../types/Job';

export interface JobCreateRequest {
    jobTitle: string;
    description: string;
    responsibilities: string;
    qualifications: string;
    companyName: string;
    location: string;
    remote: boolean;
    workMode: 'ONSITE' | 'REMOTE' | 'HYBRID';
    minYearsExperience?: number;
    maxYearsExperience?: number;
    experienceLevel?: string;
    minSalary?: number;
    maxSalary?: number;
    currency?: string;
    salaryPeriod?: 'HOURLY' | 'MONTHLY' | 'ANNUAL';
    jobType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP' | 'TEMPORARY' | 'FREELANCE';
    skills: Array<{ skillName: string; isRequired: boolean }>;
    applicationDeadline?: string;
}

export interface JobSearchParams {
    query?: string;
    location?: string;
    remote?: boolean;
    minSalary?: number;
    maxSalary?: number;
    experienceLevel?: string;
    skills?: string[];
    page?: number;
    size?: number;
}

export interface JobQualityAnalysis {
    qualityScore: number;
    completenessScore: number;
    specificityScore: number;
    realismScore: number;
    clarityScore: number;
    suggestions: string[];
}

export interface BiasAnalysis {
    biasScore: number;
    issues: Array<{
        type: string;
        severity: 'HIGH' | 'MEDIUM' | 'LOW';
        phrase: string;
        context: string;
        suggestion: string;
    }>;
}

class JobService {
    async createJob(jobData: JobCreateRequest): Promise<Job> {
        const response = await apiClient.post<Job>('/jobs', jobData);
        return response.data;
    }

    async getJob(id: string): Promise<Job> {
        const response = await apiClient.get<Job>(`/jobs/${id}`);
        return response.data;
    }

    async searchJobs(params: JobSearchParams): Promise<{ jobs: Job[], total: number }> {
        const response = await apiClient.get('/jobs/search', { params });
        return response.data;
    }

    async getMyJobs(): Promise<Job[]> {
        const response = await apiClient.get<Job[]>('/jobs/my-jobs');
        return response.data;
    }

    async updateJob(id: string, jobData: Partial<JobCreateRequest>): Promise<Job> {
        const response = await apiClient.put<Job>(`/jobs/${id}`, jobData);
        return response.data;
    }

    async deleteJob(id: string): Promise<void> {
        await apiClient.delete(`/jobs/${id}`);
    }

    async analyzeJobQuality(jobData: JobCreateRequest): Promise<JobQualityAnalysis> {
        const response = await apiClient.post<JobQualityAnalysis>('/jobs/analyze-quality', jobData);
        return response.data;
    }

    async detectBias(jobData: JobCreateRequest): Promise<BiasAnalysis> {
        const response = await apiClient.post<BiasAnalysis>('/jobs/detect-bias', jobData);
        return response.data;
    }

    async updateJobStatus(id: string, status: string): Promise<Job> {
        const response = await apiClient.patch<Job>(`/jobs/${id}/status`, { status });
        return response.data;
    }
}

export default new JobService();
