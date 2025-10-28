import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, ProgressBar, Spinner, Alert, ListGroup, Accordion } from 'react-bootstrap';
import { useParams, useNavigate } from 'react-router-dom';
import { FiMapPin, FiBriefcase, FiDollarSign, FiClock, FiArrowLeft, FiCheckCircle, FiAlertCircle } from 'react-icons/fi';
import jobService from '../services/jobService';
import matchingService, { MatchResult } from '../services/matchingService';
import { Job } from '../types/Job';
import MatchExplanation from '../components/MatchExplanation';
import SkillGapAnalysis from '../components/SkillGapAnalysis';

const JobDetails: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [job, setJob] = useState<Job | null>(null);
    const [matchResult, setMatchResult] = useState<MatchResult | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadJobDetails();
    }, [id]);

    const loadJobDetails = async () => {
        if (!id) return;

        setLoading(true);
        setError('');

        try {
            const jobData = await jobService.getJob(id);
            setJob(jobData);

            // TODO: Get current user's resume ID and fetch match details
            // For now, we'll skip the match result
            // const match = await matchingService.getDetailedMatch(id, resumeId);
            // setMatchResult(match);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load job details');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Container className="py-5 text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </Spinner>
            </Container>
        );
    }

    if (error || !job) {
        return (
            <Container className="py-5">
                <Alert variant="danger">
                    {error || 'Job not found'}
                </Alert>
                <Button variant="primary" onClick={() => navigate('/jobs')}>
                    <FiArrowLeft className="me-2" />
                    Back to Jobs
                </Button>
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <Button variant="link" onClick={() => navigate('/jobs')} className="mb-3 ps-0">
                <FiArrowLeft className="me-2" />
                Back to Jobs
            </Button>

            <Row>
                {/* Main Content */}
                <Col md={8}>
                    <Card className="border-0 shadow-sm mb-4">
                        <Card.Body className="p-4">
                            <h2 className="fw-bold mb-3">{job.title}</h2>

                            <div className="mb-3">
                                <p className="mb-2">
                                    <FiBriefcase className="me-2" />
                                    <strong>{job.company?.name}</strong>
                                </p>
                                <p className="mb-2 text-muted">
                                    <FiMapPin className="me-2" />
                                    {job.company?.location}
                                    {job.matchScore && (
                                        <Badge bg="success" className="ms-3">
                                            {(job.matchScore * 100).toFixed(0)}% Match
                                        </Badge>
                                    )}
                                </p>
                            </div>

                            <div className="mb-4">
                                <h5 className="fw-bold mb-3">About the Role</h5>
                                <p>{job.description}</p>
                            </div>

                            <div className="mb-4">
                                <h5 className="fw-bold mb-3">Required Skills</h5>
                                <div>
                                    {job.skills?.map((skill, index) => (
                                        <Badge key={index} bg="primary" className="me-2 mb-2 p-2">
                                            {skill}
                                        </Badge>
                                    ))}
                                </div>
                            </div>

                            <div className="d-grid gap-2">
                                <Button variant="primary" size="lg">
                                    Apply Now
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>

                    {/* Match Explanation */}
                    {matchResult && (
                        <>
                            <MatchExplanation explanation={matchResult.explanation} />
                            <SkillGapAnalysis analysis={matchResult.skillGapAnalysis} />
                        </>
                    )}
                </Col>

                {/* Sidebar */}
                <Col md={4}>
                    <Card className="border-0 shadow-sm mb-4">
                        <Card.Body className="p-4">
                            <h5 className="fw-bold mb-3">Job Details</h5>

                            <ListGroup variant="flush">
                                <ListGroup.Item className="px-0 border-0">
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Experience</span>
                                        <strong>3-5 years</strong>
                                    </div>
                                </ListGroup.Item>

                                <ListGroup.Item className="px-0 border-0">
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Job Type</span>
                                        <strong>Full-time</strong>
                                    </div>
                                </ListGroup.Item>

                                <ListGroup.Item className="px-0 border-0">
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Work Mode</span>
                                        <strong>Remote</strong>
                                    </div>
                                </ListGroup.Item>

                                <ListGroup.Item className="px-0 border-0">
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Salary</span>
                                        <strong>$100k - $150k</strong>
                                    </div>
                                </ListGroup.Item>

                                <ListGroup.Item className="px-0 border-0">
                                    <div className="d-flex justify-content-between">
                                        <span className="text-muted">Posted</span>
                                        <strong>2 days ago</strong>
                                    </div>
                                </ListGroup.Item>
                            </ListGroup>
                        </Card.Body>
                    </Card>

                    <Card className="border-0 shadow-sm">
                        <Card.Body className="p-4">
                            <h5 className="fw-bold mb-3">About the Company</h5>
                            <p className="text-muted">
                                {job.company?.name} is a leading technology company focused on innovation and growth.
                            </p>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default JobDetails;
