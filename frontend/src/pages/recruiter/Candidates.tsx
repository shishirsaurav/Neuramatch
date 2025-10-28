import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, ButtonGroup, Spinner } from 'react-bootstrap';
import { useSearchParams, Link } from 'react-router-dom';
import { FiUser, FiMail, FiPhone, FiCheckCircle, FiX } from 'react-icons/fi';
import matchingService, { MatchResult } from '../../services/matchingService';

const Candidates: React.FC = () => {
    const [searchParams] = useSearchParams();
    const jobId = searchParams.get('jobId');
    const [matches, setMatches] = useState<MatchResult[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (jobId) {
            loadCandidates();
        }
    }, [jobId]);

    const loadCandidates = async () => {
        if (!jobId) return;

        setLoading(true);
        try {
            const results = await matchingService.getMatchesForJob(jobId, 50);
            setMatches(results);
        } catch (err) {
            console.error('Failed to load candidates');
        } finally {
            setLoading(false);
        }
    };

    const handleFeedback = async (resumeId: string, action: 'shortlisted' | 'rejected' | 'interviewed') => {
        if (!jobId) return;

        try {
            await matchingService.submitFeedback({
                jobId,
                resumeId,
                action
            });
            alert(`Candidate ${action}!`);
        } catch (err) {
            alert('Failed to submit feedback');
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
                    <h2 className="fw-bold mb-2">Matched Candidates</h2>
                    <p className="text-muted">AI-powered candidate matches for your job posting</p>
                </Col>
            </Row>

            {matches.length === 0 ? (
                <Card className="border-0 shadow-sm text-center py-5">
                    <Card.Body>
                        <h5>No candidates found</h5>
                        <p className="text-muted">Check back later for new matches</p>
                    </Card.Body>
                </Card>
            ) : (
                <Row className="g-4">
                    {matches.map(match => (
                        <Col md={12} key={match.resumeId}>
                            <Card className="border-0 shadow-sm hover-shadow">
                                <Card.Body className="p-4">
                                    <Row>
                                        <Col md={8}>
                                            <div className="d-flex align-items-center mb-3">
                                                <div className="bg-primary bg-opacity-10 p-3 rounded-circle me-3">
                                                    <FiUser size={24} className="text-primary" />
                                                </div>
                                                <div>
                                                    <h5 className="fw-bold mb-1">Candidate #{match.resumeId.substring(0, 8)}</h5>
                                                    <div className="text-muted small">
                                                        <FiMail className="me-2" />candidate@example.com
                                                        <span className="mx-2">•</span>
                                                        <FiPhone className="me-2" />+1 (555) 123-4567
                                                    </div>
                                                </div>
                                            </div>

                                            {/* Skills */}
                                            <div className="mb-3">
                                                <h6 className="fw-semibold mb-2">Skills</h6>
                                                <div>
                                                    {match.skillGapAnalysis.matchedSkills.slice(0, 8).map((skill, i) => (
                                                        <Badge key={i} bg="success" className="me-1 mb-1">
                                                            {skill}
                                                        </Badge>
                                                    ))}
                                                </div>
                                            </div>

                                            {/* Match Reasons */}
                                            {match.explanation.keyReasons.slice(0, 2).map((reason, i) => (
                                                <div key={i} className="small text-muted mb-2">
                                                    <FiCheckCircle className="me-2 text-success" />
                                                    {reason.evidence}
                                                </div>
                                            ))}
                                        </Col>

                                        <Col md={4} className="text-end">
                                            <div className="mb-4">
                                                <div className="display-5 fw-bold text-success">
                                                    {(match.matchScore * 100).toFixed(0)}%
                                                </div>
                                                <small className="text-muted">Match Score</small>
                                                <div className="mt-2">
                                                    <Badge bg="info">
                                                        Confidence: {(match.explanation.confidence * 100).toFixed(0)}%
                                                    </Badge>
                                                </div>
                                            </div>

                                            <ButtonGroup className="w-100 mb-2">
                                                <Button
                                                    variant="success"
                                                    size="sm"
                                                    onClick={() => handleFeedback(match.resumeId, 'shortlisted')}
                                                >
                                                    <FiCheckCircle className="me-1" />
                                                    Shortlist
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => handleFeedback(match.resumeId, 'rejected')}
                                                >
                                                    <FiX />
                                                </Button>
                                            </ButtonGroup>
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="w-100"
                                            >
                                                View Full Profile
                                            </Button>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
};

export default Candidates;
