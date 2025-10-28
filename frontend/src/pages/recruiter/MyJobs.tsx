import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FiBriefcase, FiMapPin, FiEye, FiEdit, FiTrash2 } from 'react-icons/fi';
import jobService from '../../services/jobService';
import { Job } from '../../types/Job';

const MyJobs: React.FC = () => {
    const [jobs, setJobs] = useState<Job[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadJobs();
    }, []);

    const loadJobs = async () => {
        setLoading(true);
        try {
            const myJobs = await jobService.getMyJobs();
            setJobs(myJobs);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load jobs');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: string) => {
        if (window.confirm('Are you sure you want to delete this job?')) {
            try {
                await jobService.deleteJob(id);
                setJobs(jobs.filter(job => job.id !== id));
            } catch (err: any) {
                alert('Failed to delete job');
            }
        }
    };

    if (loading) {
        return (
            <Container className="py-5 text-center">
                <Spinner animation="border" />
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <div className="d-flex justify-content-between align-items-center">
                        <div>
                            <h2 className="fw-bold mb-2">My Job Postings</h2>
                            <p className="text-muted">Manage your active and past job postings</p>
                        </div>
                        <Link to="/recruiter/post-job">
                            <Button variant="primary">Post New Job</Button>
                        </Link>
                    </div>
                </Col>
            </Row>

            {error && <Alert variant="danger">{error}</Alert>}

            {jobs.length === 0 ? (
                <Card className="border-0 shadow-sm text-center py-5">
                    <Card.Body>
                        <FiBriefcase size={48} className="text-muted mb-3" />
                        <h5>No job postings yet</h5>
                        <p className="text-muted mb-3">Create your first job posting to start finding candidates</p>
                        <Link to="/recruiter/post-job">
                            <Button variant="primary">Post a Job</Button>
                        </Link>
                    </Card.Body>
                </Card>
            ) : (
                <Row className="g-4">
                    {jobs.map(job => (
                        <Col md={6} lg={4} key={job.id}>
                            <Card className="border-0 shadow-sm h-100 hover-shadow">
                                <Card.Body>
                                    <div className="d-flex justify-content-between align-items-start mb-3">
                                        <h5 className="fw-bold mb-0">{job.title}</h5>
                                        <Badge bg="success">Active</Badge>
                                    </div>
                                    <p className="mb-2 text-muted">
                                        <FiBriefcase className="me-2" size={14} />
                                        {job.company?.name}
                                    </p>
                                    <p className="mb-3 text-muted">
                                        <FiMapPin className="me-2" size={14} />
                                        {job.company?.location}
                                    </p>
                                    <div className="mb-3">
                                        {job.skills?.slice(0, 3).map((skill, i) => (
                                            <Badge key={i} bg="secondary" className="me-1 mb-1">{skill}</Badge>
                                        ))}
                                    </div>
                                    <div className="d-flex gap-2">
                                        <Link to={`/recruiter/candidates?jobId=${job.id}`} className="flex-grow-1">
                                            <Button variant="primary" size="sm" className="w-100">
                                                <FiEye className="me-2" />
                                                View Candidates
                                            </Button>
                                        </Link>
                                        <Button variant="outline-danger" size="sm" onClick={() => job.id && handleDelete(job.id)}>
                                            <FiTrash2 />
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default MyJobs;
