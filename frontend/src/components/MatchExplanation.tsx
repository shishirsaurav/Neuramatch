import React from 'react';
import { Card, ProgressBar, Badge, ListGroup, Row, Col } from 'react-bootstrap';
import { FiCheckCircle, FiInfo } from 'react-icons/fi';
import { MatchExplanation as MatchExplanationType } from '../services/matchingService';

interface MatchExplanationProps {
    explanation: MatchExplanationType;
}

const MatchExplanation: React.FC<MatchExplanationProps> = ({ explanation }) => {
    const {
        overallScore,
        breakdown,
        keyReasons,
        confidence
    } = explanation;

    const getScoreColor = (score: number): string => {
        if (score >= 0.8) return 'success';
        if (score >= 0.6) return 'info';
        if (score >= 0.4) return 'warning';
        return 'danger';
    };

    const formatFactorName = (factor: string): string => {
        return factor
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    };

    return (
        <Card className="border-0 shadow-sm mb-4">
            <Card.Body className="p-4">
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h5 className="fw-bold mb-0">
                        <FiInfo className="me-2" />
                        Match Analysis
                    </h5>
                    <div className="text-end">
                        <div className="fs-3 fw-bold text-success">
                            {(overallScore * 100).toFixed(0)}%
                        </div>
                        <Badge bg="light" text="dark">
                            Confidence: {(confidence * 100).toFixed(0)}%
                        </Badge>
                    </div>
                </div>

                {/* Score Breakdown */}
                <div className="mb-4">
                    <h6 className="fw-semibold mb-3">Score Breakdown</h6>
                    <Row>
                        {Object.entries(breakdown).map(([factor, score]) => (
                            <Col md={6} key={factor} className="mb-3">
                                <div className="d-flex justify-content-between mb-1">
                                    <small className="text-muted">{formatFactorName(factor)}</small>
                                    <small className="fw-semibold">{(score * 100).toFixed(0)}%</small>
                                </div>
                                <ProgressBar
                                    now={score * 100}
                                    variant={getScoreColor(score)}
                                    style={{ height: '8px' }}
                                />
                            </Col>
                        ))}
                    </Row>
                </div>

                {/* Key Reasons */}
                <div>
                    <h6 className="fw-semibold mb-3">Why This Match?</h6>
                    <ListGroup variant="flush">
                        {keyReasons.map((reason, index) => (
                            <ListGroup.Item key={index} className="px-0 border-0 py-2">
                                <div className="d-flex align-items-start">
                                    <FiCheckCircle
                                        className={`me-3 mt-1 text-${getScoreColor(reason.score)}`}
                                        size={20}
                                    />
                                    <div className="flex-grow-1">
                                        <div className="d-flex justify-content-between align-items-start mb-1">
                                            <strong className="text-capitalize">
                                                {formatFactorName(reason.factor)}
                                            </strong>
                                            <Badge bg={getScoreColor(reason.score)}>
                                                {(reason.score * 100).toFixed(0)}%
                                            </Badge>
                                        </div>
                                        <p className="mb-0 text-muted small">{reason.evidence}</p>
                                    </div>
                                </div>
                            </ListGroup.Item>
                        ))}
                    </ListGroup>
                </div>

                {/* AI Confidence Note */}
                <div className="mt-4 p-3 bg-light rounded">
                    <small className="text-muted">
                        <strong>Note:</strong> This match analysis is powered by AI and considers multiple factors including
                        technical skills, experience level, domain expertise, education, and skill recency. The confidence score
                        indicates how certain our AI is about this prediction.
                    </small>
                </div>
            </Card.Body>
        </Card>
    );
};

export default MatchExplanation;
