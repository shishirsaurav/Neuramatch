import apiClient from './api';

export interface TrendingSkill {
    skillName: string;
    trendScore: number;
    growthRate: number;
    demandCount: number;
    category: string;
}

export interface SalaryBenchmark {
    skillName: string;
    avgSalary: number;
    minSalary: number;
    maxSalary: number;
    currency: string;
    sampleSize: number;
}

export interface HiringFunnelMetrics {
    totalApplications: number;
    shortlisted: number;
    interviewed: number;
    offered: number;
    hired: number;
    avgTimeToHire: number;
    conversionRates: {
        applicationToShortlist: number;
        shortlistToInterview: number;
        interviewToOffer: number;
        offerToHire: number;
    };
}

export interface DiversityMetrics {
    totalCandidates: number;
    byExperienceLevel: Record<string, number>;
    byLocation: Record<string, number>;
    avgMatchScore: number;
}

class AnalyticsService {
    async getTrendingSkills(category?: string, limit: number = 20): Promise<TrendingSkill[]> {
        const response = await apiClient.get<TrendingSkill[]>('/analytics/trending-skills', {
            params: { category, limit }
        });
        return response.data;
    }

    async getSalaryBenchmarks(skills: string[]): Promise<SalaryBenchmark[]> {
        const response = await apiClient.post<SalaryBenchmark[]>('/analytics/salary-benchmarks', { skills });
        return response.data;
    }

    async getHiringFunnelMetrics(jobId?: string, startDate?: string, endDate?: string): Promise<HiringFunnelMetrics> {
        const response = await apiClient.get<HiringFunnelMetrics>('/analytics/hiring-funnel', {
            params: { jobId, startDate, endDate }
        });
        return response.data;
    }

    async getDiversityMetrics(jobId?: string): Promise<DiversityMetrics> {
        const response = await apiClient.get<DiversityMetrics>('/analytics/diversity', {
            params: { jobId }
        });
        return response.data;
    }

    async getMatchQualityMetrics(): Promise<{
        avgMatchScore: number;
        avgConfidence: number;
        totalMatches: number;
        successfulHires: number;
    }> {
        const response = await apiClient.get('/analytics/match-quality');
        return response.data;
    }
}

export default new AnalyticsService();
