import React, { useState } from 'react';
import { Form, Button, Card, ProgressBar, Alert } from 'react-bootstrap';
import axios from 'axios';
import { Resume } from '../types/Resume';
import { Job } from '../types/Job';

interface ResumeUploadProps {
    onUploadSuccess: (resume: Resume, jobs: Job[]) => void;
}

const ResumeUpload: React.FC<ResumeUploadProps> = ({ onUploadSuccess }) => {
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [error, setError] = useState<string | null>(null);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files) {
            setFile(event.target.files[0]);
            setError(null);
        }
    };

    const handleUpload = async () => {
        if (!file) {
            return;
        }

        setUploading(true);
        setUploadProgress(0);
        setError(null);
        const formData = new FormData();
        formData.append('file', file);

        // --- MOCK DATA FOR UI DEVELOPMENT ---
        // In a real scenario, the backend would return this data.
        const mockResume: Resume = {
            fileName: 'JohnDoe_Resume.pdf',
            name: 'John Doe',
            email: 'john.doe@example.com',
            phone: '123-456-7890',
            summary: 'A highly motivated software engineer with 5 years of experience in full-stack web development, specializing in React and Node.js.',
            skills: [{ name: 'React' }, { name: 'Node.js' }, { name: 'TypeScript' }, { name: 'JavaScript' }, { name: 'HTML/CSS' }],
            experience: [
                {
                    jobTitle: 'Senior Software Engineer',
                    company: 'Tech Solutions Inc.',
                    location: 'San Francisco, CA',
                    startDate: 'Jan 2021',
                    endDate: null,
                    description: 'Led the development of a new client-facing dashboard using React and TypeScript.'
                }
            ],
            education: [
                {
                    institution: 'University of California, Berkeley',
                    degree: 'Bachelor of Science',
                    fieldOfStudy: 'Computer Science',
                    startDate: 'Aug 2014',
                    endDate: 'May 2018'
                }
            ]
        };
        const mockJobs: Job[] = [
            {
                id: '1',
                title: 'Frontend Developer',
                company: { name: 'Innovate Corp', location: 'Palo Alto, CA' },
                description: 'Exciting opportunity for a frontend developer...',
                skills: ['React', 'TypeScript', 'Redux'],
                matchScore: 0.92
            },
            {
                id: '2',
                title: 'Full Stack Engineer',
                company: { name: 'Data Systems', location: 'New York, NY' },
                description: 'Join our full-stack team...',
                skills: ['React', 'Node.js', 'PostgreSQL'],
                matchScore: 0.88
            }
        ];

        // Simulate API call delay
        setTimeout(() => {
            onUploadSuccess(mockResume, mockJobs);
            setUploading(false);
        }, 2000);
        // --- END MOCK DATA ---

        /* --- REAL API CALL (commented out for now) ---
        try {
            const response = await axios.post('/api/v1/resumes/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
                onUploadProgress: (progressEvent) => {
                    const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
                    setUploadProgress(percentCompleted);
                },
            });
            // The backend should return a combined payload of resume details and job matches
            const { resume, jobs } = response.data;
            onUploadSuccess(resume, jobs);
        } catch (err) {
            setError('Failed to upload and process resume. Please try again.');
            console.error('Error uploading file:', err);
        } finally {
            setUploading(false);
        }
        */
    };

    return (
        <Card className="p-4 p-sm-5">
            <Card.Body>
                <Card.Title as="h2" className="text-center mb-4">Upload Your Resume</Card.Title>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form>
                    <Form.Group controlId="formFileLg" className="mb-3">
                        <Form.Control type="file" size="lg" onChange={handleFileChange} accept=".pdf,.doc,.docx" />
                    </Form.Group>

                    {uploading && (
                        <ProgressBar animated now={uploadProgress} label={`${uploadProgress}%`} className="mb-3" />
                    )}

                    <div className="d-grid">
                        <Button
                            variant="primary"
                            size="lg"
                            onClick={handleUpload}
                            disabled={!file || uploading}
                        >
                            {uploading ? 'Analyzing...' : 'Find My Match'}
                        </Button>
                    </div>
                </Form>
            </Card.Body>
        </Card>
    );
};

export default ResumeUpload;
