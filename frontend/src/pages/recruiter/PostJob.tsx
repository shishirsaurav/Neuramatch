import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Badge, InputGroup } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { FiBriefcase, FiMapPin, FiDollarSign, FiCheckCircle, FiAlertCircle } from 'react-icons/fi';
import jobService, { JobCreateRequest, JobQualityAnalysis, BiasAnalysis } from '../../services/jobService';

const PostJob: React.FC = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [qualityAnalysis, setQualityAnalysis] = useState<JobQualityAnalysis | null>(null);
    const [biasAnalysis, setBiasAnalysis] = useState<BiasAnalysis | null>(null);

    const [formData, setFormData] = useState<JobCreateRequest>({
        jobTitle: '',
        description: '',
        responsibilities: '',
        qualifications: '',
        companyName: '',
        location: '',
        remote: false,
        workMode: 'ONSITE',
        minYearsExperience: 0,
        maxYearsExperience: 10,
        experienceLevel: 'MID',
        minSalary: undefined,
        maxSalary: undefined,
        currency: 'USD',
        salaryPeriod: 'ANNUAL',
        jobType: 'FULL_TIME',
        skills: [],
        applicationDeadline: undefined,
    });

    const [skillInput, setSkillInput] = useState('');

    const handleChange = (field: keyof JobCreateRequest, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const addSkill = () => {
        if (skillInput.trim()) {
            setFormData(prev => ({
                ...prev,
                skills: [...prev.skills, { skillName: skillInput.trim(), isRequired: true }]
            }));
            setSkillInput('');
        }
    };

    const removeSkill = (index: number) => {
        setFormData(prev => ({
            ...prev,
            skills: prev.skills.filter((_, i) => i !== index)
        }));
    };

    const analyzeQuality = async () => {
        try {
            const [quality, bias] = await Promise.all([
                jobService.analyzeJobQuality(formData),
                jobService.detectBias(formData)
            ]);
            setQualityAnalysis(quality);
            setBiasAnalysis(bias);
        } catch (err: any) {
            console.error('Analysis failed:', err);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await jobService.createJob(formData);
            setSuccess(true);
            setTimeout(() => {
                navigate('/recruiter/my-jobs');
            }, 2000);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to create job posting');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <h2 className="fw-bold mb-2">Post a New Job</h2>
                    <p className="text-muted">Create a job posting with AI-powered quality checks</p>
                </Col>
            </Row>

            {success && (
                <Alert variant="success" className="mb-4">
                    <FiCheckCircle className="me-2" />
                    Job posted successfully! Redirecting...
                </Alert>
            )}

            {error && <Alert variant="danger" className="mb-4">{error}</Alert>}

            <Row>
                <Col md={8}>
                    <Card className="border-0 shadow-sm mb-4">
                        <Card.Body className="p-4">
                            <Form onSubmit={handleSubmit}>
                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Job Title *</Form.Label>
                                    <InputGroup>
                                        <InputGroup.Text><FiBriefcase /></InputGroup.Text>
                                        <Form.Control
                                            type="text"
                                            placeholder="e.g., Senior Software Engineer"
                                            value={formData.jobTitle}
                                            onChange={(e) => handleChange('jobTitle', e.target.value)}
                                            required
                                        />
                                    </InputGroup>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Company Name *</Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Your company name"
                                        value={formData.companyName}
                                        onChange={(e) => handleChange('companyName', e.target.value)}
                                        required
                                    />
                                </Form.Group>

                                <Row>
                                    <Col md={8}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Location *</Form.Label>
                                            <InputGroup>
                                                <InputGroup.Text><FiMapPin /></InputGroup.Text>
                                                <Form.Control
                                                    type="text"
                                                    placeholder="City, State"
                                                    value={formData.location}
                                                    onChange={(e) => handleChange('location', e.target.value)}
                                                    required
                                                />
                                            </InputGroup>
                                        </Form.Group>
                                    </Col>
                                    <Col md={4}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Work Mode *</Form.Label>
                                            <Form.Select
                                                value={formData.workMode}
                                                onChange={(e) => handleChange('workMode', e.target.value)}
                                            >
                                                <option value="ONSITE">On-site</option>
                                                <option value="REMOTE">Remote</option>
                                                <option value="HYBRID">Hybrid</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Job Description *</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={4}
                                        placeholder="Describe the role..."
                                        value={formData.description}
                                        onChange={(e) => handleChange('description', e.target.value)}
                                        required
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Responsibilities</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={3}
                                        placeholder="Key responsibilities..."
                                        value={formData.responsibilities}
                                        onChange={(e) => handleChange('responsibilities', e.target.value)}
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Qualifications</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={3}
                                        placeholder="Required qualifications..."
                                        value={formData.qualifications}
                                        onChange={(e) => handleChange('qualifications', e.target.value)}
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Required Skills</Form.Label>
                                    <InputGroup className="mb-2">
                                        <Form.Control
                                            type="text"
                                            placeholder="Add a skill..."
                                            value={skillInput}
                                            onChange={(e) => setSkillInput(e.target.value)}
                                            onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addSkill())}
                                        />
                                        <Button variant="outline-primary" onClick={addSkill}>Add</Button>
                                    </InputGroup>
                                    <div>
                                        {formData.skills.map((skill, index) => (
                                            <Badge
                                                key={index}
                                                bg="primary"
                                                className="me-2 mb-2 p-2"
                                                style={{ cursor: 'pointer' }}
                                                onClick={() => removeSkill(index)}
                                            >
                                                {skill.skillName} ×
                                            </Badge>
                                        ))}
                                    </div>
                                </Form.Group>

                                <Row>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Experience Level</Form.Label>
                                            <Form.Select
                                                value={formData.experienceLevel}
                                                onChange={(e) => handleChange('experienceLevel', e.target.value)}
                                            >
                                                <option value="ENTRY">Entry Level</option>
                                                <option value="MID">Mid Level</option>
                                                <option value="SENIOR">Senior Level</option>
                                                <option value="LEAD">Lead</option>
                                                <option value="PRINCIPAL">Principal</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Job Type</Form.Label>
                                            <Form.Select
                                                value={formData.jobType}
                                                onChange={(e) => handleChange('jobType', e.target.value)}
                                            >
                                                <option value="FULL_TIME">Full-time</option>
                                                <option value="PART_TIME">Part-time</option>
                                                <option value="CONTRACT">Contract</option>
                                                <option value="INTERNSHIP">Internship</option>
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <Row>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Min Salary</Form.Label>
                                            <InputGroup>
                                                <InputGroup.Text><FiDollarSign /></InputGroup.Text>
                                                <Form.Control
                                                    type="number"
                                                    placeholder="50000"
                                                    value={formData.minSalary || ''}
                                                    onChange={(e) => handleChange('minSalary', e.target.value ? parseInt(e.target.value) : undefined)}
                                                />
                                            </InputGroup>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Max Salary</Form.Label>
                                            <InputGroup>
                                                <InputGroup.Text><FiDollarSign /></InputGroup.Text>
                                                <Form.Control
                                                    type="number"
                                                    placeholder="100000"
                                                    value={formData.maxSalary || ''}
                                                    onChange={(e) => handleChange('maxSalary', e.target.value ? parseInt(e.target.value) : undefined)}
                                                />
                                            </InputGroup>
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <div className="d-flex gap-2">
                                    <Button
                                        variant="outline-primary"
                                        onClick={analyzeQuality}
                                        type="button"
                                    >
                                        Analyze Quality
                                    </Button>
                                    <Button
                                        variant="primary"
                                        type="submit"
                                        disabled={loading}
                                        className="flex-grow-1"
                                    >
                                        {loading ? 'Posting...' : 'Post Job'}
                                    </Button>
                                </div>
                            </Form>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={4}>
                    {qualityAnalysis && (
                        <Card className="border-0 shadow-sm mb-3">
                            <Card.Body>
                                <h6 className="fw-bold mb-3">Quality Score</h6>
                                <div className="text-center mb-3">
                                    <div className="display-4 fw-bold text-primary">
                                        {qualityAnalysis.qualityScore}
                                    </div>
                                    <small className="text-muted">out of 100</small>
                                </div>
                                {qualityAnalysis.suggestions.length > 0 && (
                                    <>
                                        <h6 className="fw-semibold mb-2">Suggestions:</h6>
                                        <ul className="small">
                                            {qualityAnalysis.suggestions.map((s, i) => (
                                                <li key={i}>{s}</li>
                                            ))}
                                        </ul>
                                    </>
                                )}
                            </Card.Body>
                        </Card>
                    )}

                    {biasAnalysis && biasAnalysis.issues.length > 0 && (
                        <Card className="border-0 shadow-sm border-warning">
                            <Card.Body>
                                <h6 className="fw-bold mb-3 text-warning">
                                    <FiAlertCircle className="me-2" />
                                    Bias Detected
                                </h6>
                                {biasAnalysis.issues.map((issue, i) => (
                                    <div key={i} className="mb-3">
                                        <Badge bg={issue.severity === 'HIGH' ? 'danger' : 'warning'} className="mb-1">
                                            {issue.severity}
                                        </Badge>
                                        <p className="small mb-1"><strong>{issue.phrase}</strong></p>
                                        <p className="small text-muted">{issue.suggestion}</p>
                                    </div>
                                ))}
                            </Card.Body>
                        </Card>
                    )}
                </Col>
            </Row>
        </Container>
    );
};

export default PostJob;
