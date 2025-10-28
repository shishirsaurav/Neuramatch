import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Badge, InputGroup, Spinner, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FiSearch, FiMapPin, FiDollarSign, FiBriefcase, FiFilter } from 'react-icons/fi';
import jobService, { JobSearchParams } from '../services/jobService';
import { Job } from '../types/Job';

const JobSearch: React.FC = () => {
    const [jobs, setJobs] = useState<Job[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [filters, setFilters] = useState<JobSearchParams>({
        query: '',
        location: '',
        remote: undefined,
        minSalary: undefined,
        experienceLevel: '',
        page: 0,
        size: 20,
    });

    useEffect(() => {
        searchJobs();
    }, []);

    const searchJobs = async () => {
        setLoading(true);
        setError('');
        try {
            const response = await jobService.searchJobs(filters);
            setJobs(response.jobs);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load jobs');
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        searchJobs();
    };

    const handleFilterChange = (field: keyof JobSearchParams, value: any) => {
        setFilters(prev => ({ ...prev, [field]: value }));
    };

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <h2 className="fw-bold mb-3">Find Your Perfect Job</h2>
                    <p className="text-muted">Browse thousands of opportunities matched to your skills</p>
                </Col>
            </Row>

            <Row>
                {/* Filters Sidebar */}
                <Col md={3} className="mb-4">
                    <Card className="border-0 shadow-sm sticky-top" style={{ top: '20px' }}>
                        <Card.Body>
                            <h5 className="fw-bold mb-3">
                                <FiFilter className="me-2" />
                                Filters
                            </h5>

                            <Form>
                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Location</Form.Label>
                                    <InputGroup>
                                        <InputGroup.Text>
                                            <FiMapPin />
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="text"
                                            placeholder="City, State"
                                            value={filters.location || ''}
                                            onChange={(e) => handleFilterChange('location', e.target.value)}
                                        />
                                    </InputGroup>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Check
                                        type="checkbox"
                                        label="Remote Only"
                                        checked={filters.remote || false}
                                        onChange={(e) => handleFilterChange('remote', e.target.checked)}
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Experience Level</Form.Label>
                                    <Form.Select
                                        value={filters.experienceLevel || ''}
                                        onChange={(e) => handleFilterChange('experienceLevel', e.target.value)}
                                    >
                                        <option value="">All Levels</option>
                                        <option value="ENTRY">Entry Level</option>
                                        <option value="MID">Mid Level</option>
                                        <option value="SENIOR">Senior Level</option>
                                        <option value="LEAD">Lead</option>
                                        <option value="PRINCIPAL">Principal</option>
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Minimum Salary</Form.Label>
                                    <InputGroup>
                                        <InputGroup.Text>
                                            <FiDollarSign />
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="number"
                                            placeholder="50000"
                                            value={filters.minSalary || ''}
                                            onChange={(e) => handleFilterChange('minSalary', e.target.value ? parseInt(e.target.value) : undefined)}
                                        />
                                    </InputGroup>
                                </Form.Group>

                                <Button variant="primary" className="w-100" onClick={searchJobs}>
                                    Apply Filters
                                </Button>
                            </Form>
                        </Card.Body>
                    </Card>
                </Col>

                {/* Job Listings */}
                <Col md={9}>
                    {/* Search Bar */}
                    <Card className="border-0 shadow-sm mb-4">
                        <Card.Body>
                            <Form onSubmit={handleSearch}>
                                <InputGroup size="lg">
                                    <InputGroup.Text>
                                        <FiSearch />
                                    </InputGroup.Text>
                                    <Form.Control
                                        type="text"
                                        placeholder="Search by job title, skills, or company..."
                                        value={filters.query || ''}
                                        onChange={(e) => handleFilterChange('query', e.target.value)}
                                    />
                                    <Button variant="primary" type="submit">
                                        Search
                                    </Button>
                                </InputGroup>
                            </Form>
                        </Card.Body>
                    </Card>

                    {/* Loading State */}
                    {loading && (
                        <div className="text-center py-5">
                            <Spinner animation="border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </Spinner>
                            <p className="mt-3 text-muted">Finding the best matches...</p>
                        </div>
                    )}

                    {/* Error State */}
                    {error && <Alert variant="danger">{error}</Alert>}

                    {/* Job Results */}
                    {!loading && jobs.length === 0 && !error && (
                        <Card className="border-0 shadow-sm text-center py-5">
                            <Card.Body>
                                <FiBriefcase size={48} className="text-muted mb-3" />
                                <h5>No jobs found</h5>
                                <p className="text-muted">Try adjusting your filters or search terms</p>
                            </Card.Body>
                        </Card>
                    )}

                    {!loading && jobs.length > 0 && (
                        <>
                            <div className="mb-3">
                                <p className="text-muted">Found {jobs.length} jobs</p>
                            </div>

                            {jobs.map((job) => (
                                <Card key={job.id} className="border-0 shadow-sm mb-3 hover-shadow">
                                    <Card.Body className="p-4">
                                        <Row>
                                            <Col md={9}>
                                                <h5 className="fw-bold mb-2">
                                                    <Link to={`/jobs/${job.id}`} className="text-decoration-none text-dark">
                                                        {job.title}
                                                    </Link>
                                                </h5>
                                                <p className="mb-2 text-muted">
                                                    <FiBriefcase className="me-2" />
                                                    {job.company?.name}
                                                    <span className="mx-2">•</span>
                                                    <FiMapPin className="me-2" />
                                                    {job.company?.location}
                                                </p>
                                                <p className="mb-3">{job.description?.substring(0, 200)}...</p>
                                                <div>
                                                    {job.skills?.slice(0, 5).map((skill, index) => (
                                                        <Badge key={index} bg="secondary" className="me-1 mb-1">
                                                            {skill}
                                                        </Badge>
                                                    ))}
                                                    {job.skills && job.skills.length > 5 && (
                                                        <Badge bg="light" text="dark" className="me-1 mb-1">
                                                            +{job.skills.length - 5} more
                                                        </Badge>
                                                    )}
                                                </div>
                                            </Col>
                                            <Col md={3} className="text-end">
                                                {job.matchScore && (
                                                    <div className="mb-3">
                                                        <Badge bg="success" className="fs-6">
                                                            {(job.matchScore * 100).toFixed(0)}% Match
                                                        </Badge>
                                                    </div>
                                                )}
                                                <Link to={`/jobs/${job.id}`}>
                                                    <Button variant="outline-primary">View Details</Button>
                                                </Link>
                                            </Col>
                                        </Row>
                                    </Card.Body>
                                </Card>
                            ))}
                        </>
                    )}
                </Col>
            </Row>
        </Container>
    );
};

export default JobSearch;
