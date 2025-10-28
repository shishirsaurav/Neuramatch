import apiClient from './api';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    role: 'CANDIDATE' | 'RECRUITER';
}

export interface User {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    role: 'CANDIDATE' | 'RECRUITER';
}

export interface AuthResponse {
    token: string;
    user: User;
}

// Backend response structure
interface BackendAuthResponse {
    token: string;
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'CANDIDATE' | 'RECRUITER';
    message: string;
}

class AuthService {
    async login(credentials: LoginRequest): Promise<AuthResponse> {
        const response = await apiClient.post<BackendAuthResponse>('/auth/login', credentials);
        if (response.data.token) {
            localStorage.setItem('authToken', response.data.token);
            const user: User = {
                id: response.data.userId.toString(),
                email: response.data.email,
                firstName: response.data.firstName,
                lastName: response.data.lastName,
                role: response.data.role
            };
            localStorage.setItem('user', JSON.stringify(user));
            return { token: response.data.token, user };
        }
        throw new Error('No token received');
    }

    async register(userData: RegisterRequest): Promise<AuthResponse> {
        const response = await apiClient.post<BackendAuthResponse>('/auth/register', userData);
        if (response.data.token) {
            localStorage.setItem('authToken', response.data.token);
            const user: User = {
                id: response.data.userId.toString(),
                email: response.data.email,
                firstName: response.data.firstName,
                lastName: response.data.lastName,
                role: response.data.role
            };
            localStorage.setItem('user', JSON.stringify(user));
            return { token: response.data.token, user };
        }
        throw new Error('No token received');
    }

    logout(): void {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
    }

    getCurrentUser(): User | null {
        const userStr = localStorage.getItem('user');
        if (!userStr || userStr === 'undefined' || userStr === 'null') {
            return null;
        }
        try {
            return JSON.parse(userStr);
        } catch (e) {
            console.error('Error parsing user data:', e);
            return null;
        }
    }

    isAuthenticated(): boolean {
        return !!localStorage.getItem('authToken');
    }

    getToken(): string | null {
        return localStorage.getItem('authToken');
    }
}

export default new AuthService();
