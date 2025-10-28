import apiClient from './api';

export interface MatchResult {
    jobId: string;
    resumeId: string;
    matchScore: number;
    bidirectionalScore?: number;
    explanation: MatchExplanation;
    skillGapAnalysis: SkillGapAnalysis;
}

export interface MatchExplanation {
    overallScore: number;
    breakdown: {
        technical_skills: number;
        experience_level: number;
        domain_expertise: number;
        cultural_fit: number;
        education: number;
        recency: number;
    };
    keyReasons: Array<{
        factor: string;
        score: number;
        evidence: string;
    }>;
    confidence: number;
}

export interface SkillGapAnalysis {
    matchedSkills: string[];
    missingSkills: Array<{
        skillName: string;
        importance: 'HIGH' | 'MEDIUM' | 'LOW';
        transferableFrom?: string[];
    }>;
    transferableSkills: Array<{
        candidateSkill: string;
        jobSkill: string;
        transferabilityScore: number;
    }>;
    recommendedLearningPath: Array<{
        skillName: string;
        estimatedTimeWeeks: number;
        prerequisites: string[];
        resources: string[];
    }>;
}

export interface FeedbackRequest {
    jobId: string;
    resumeId: string;
    action: 'viewed' | 'shortlisted' | 'rejected' | 'interviewed' | 'offered' | 'hired';
    notes?: string;
}

export interface SkillRecommendation {
    skillName: string;
    relevanceScore: number;
    marketDemand: number;
    salaryImpact: number;
    learningPath: string[];
}

class MatchingService {
    async getMatchesForResume(resumeId: string, limit: number = 20): Promise<MatchResult[]> {
        const response = await apiClient.get<MatchResult[]>(`/match/resume/${resumeId}`, {
            params: { limit }
        });
        return response.data;
    }

    async getMatchesForJob(jobId: string, limit: number = 100): Promise<MatchResult[]> {
        const response = await apiClient.get<MatchResult[]>(`/match/job/${jobId}`, {
            params: { limit }
        });
        return response.data;
    }

    async getDetailedMatch(jobId: string, resumeId: string): Promise<MatchResult> {
        const response = await apiClient.get<MatchResult>(`/match/detailed`, {
            params: { jobId, resumeId }
        });
        return response.data;
    }

    async submitFeedback(feedback: FeedbackRequest): Promise<void> {
        await apiClient.post('/match/feedback', feedback);
    }

    async getSkillRecommendations(resumeId: string): Promise<SkillRecommendation[]> {
        const response = await apiClient.get<SkillRecommendation[]>(`/skills/recommendations/${resumeId}`);
        return response.data;
    }

    async getBidirectionalMatch(jobId: string, resumeId: string): Promise<{
        jobToResume: number;
        resumeToJob: number;
        harmonicMean: number;
        explanation: string;
    }> {
        const response = await apiClient.get(`/match/bidirectional`, {
            params: { jobId, resumeId }
        });
        return response.data;
    }
}

export default new MatchingService();
