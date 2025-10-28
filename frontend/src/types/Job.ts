export interface Company {
    name: string;
    location: string;
}

export interface Job {
    id: string;
    title: string;
    description: string;
    company: Company;
    skills: string[];
    matchScore?: number; // Optional field for matching results
}
