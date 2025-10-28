import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiUpload, FiSearch, FiBriefcase, FiTrendingUp, FiUsers, FiFileText } from 'react-icons/fi';

const Dashboard: React.FC = () => {
    const { user } = useAuth();
    const isCandidate = user?.role === 'CANDIDATE';
    const isRecruiter = user?.role === 'RECRUITER';

    return (
        <Container className="py-5">
            <div className="mb-5">
                <h1 className="display-4 fw-bold mb-2">Welcome back, {user?.firstName}!</h1>
                <p className="lead text-muted">
                    {isCandidate && "Let's find your perfect job match"}
                    {isRecruiter && "Manage your job postings and find top candidates"}
                </p>
            </div>

            {isCandidate && (
                <>
                    <Row className="g-4 mb-5">
                        <Col md={6} lg={4}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-primary">
                                        <FiUpload size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">Upload Resume</Card.Title>
                                    <Card.Text className="text-muted">
                                        Upload your resume to get AI-powered job matches and insights
                                    </Card.Text>
                                    <Link to="/upload-resume">
                                        <Button variant="primary">Get Started</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6} lg={4}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-success">
                                        <FiSearch size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">Browse Jobs</Card.Title>
                                    <Card.Text className="text-muted">
                                        Explore thousands of job opportunities tailored to your skills
                                    </Card.Text>
                                    <Link to="/jobs">
                                        <Button variant="success">Search Jobs</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6} lg={4}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-info">
                                        <FiTrendingUp size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">Skill Insights</Card.Title>
                                    <Card.Text className="text-muted">
                                        Get personalized recommendations to boost your career
                                    </Card.Text>
                                    <Link to="/analytics">
                                        <Button variant="info">View Insights</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    <Row className="mb-4">
                        <Col>
                            <Card className="border-0 shadow-sm">
                                <Card.Body className="p-4">
                                    <h5 className="fw-bold mb-3">Your Recent Matches</h5>
                                    <p className="text-muted">Upload a resume to see your personalized job matches</p>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </>
            )}

            {isRecruiter && (
                <>
                    <Row className="g-4 mb-5">
                        <Col md={6} lg={3}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-primary">
                                        <FiBriefcase size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">Post a Job</Card.Title>
                                    <Card.Text className="text-muted">
                                        Create new job postings with AI-powered quality checks
                                    </Card.Text>
                                    <Link to="/recruiter/post-job">
                                        <Button variant="primary">Create Job</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6} lg={3}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-success">
                                        <FiUsers size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">View Candidates</Card.Title>
                                    <Card.Text className="text-muted">
                                        Browse matched candidates for your job postings
                                    </Card.Text>
                                    <Link to="/recruiter/candidates">
                                        <Button variant="success">View Matches</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6} lg={3}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-warning">
                                        <FiFileText size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">My Jobs</Card.Title>
                                    <Card.Text className="text-muted">
                                        Manage your active and past job postings
                                    </Card.Text>
                                    <Link to="/recruiter/my-jobs">
                                        <Button variant="warning">Manage Jobs</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={6} lg={3}>
                            <Card className="h-100 border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <div className="mb-3 text-info">
                                        <FiTrendingUp size={40} />
                                    </div>
                                    <Card.Title className="fw-bold">Analytics</Card.Title>
                                    <Card.Text className="text-muted">
                                        View hiring metrics and market insights
                                    </Card.Text>
                                    <Link to="/analytics">
                                        <Button variant="info">View Analytics</Button>
                                    </Link>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    <Row className="mb-4">
                        <Col>
                            <Card className="border-0 shadow-sm">
                                <Card.Body className="p-4">
                                    <h5 className="fw-bold mb-3">Recent Activity</h5>
                                    <p className="text-muted">Your recent job postings and candidate activity will appear here</p>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </>
            )}
        </Container>
    );
};

export default Dashboard;
