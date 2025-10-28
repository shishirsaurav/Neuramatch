export interface Skill {
    name: string;
}

export interface Experience {
    jobTitle: string;
    company: string;
    location: string;
    startDate: string;
    endDate: string | null;
    description: string;
}

export interface Education {
    institution: string;
    degree: string;
    fieldOfStudy: string;
    startDate: string;
    endDate: string | null;
}

export interface Resume {
    fileName: string;
    name: string;
    email: string;
    phone: string;
    summary: string;
    skills: Skill[];
    experience: Experience[];
    education: Education[];
}
