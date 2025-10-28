import React from 'react';
import { Card, Badge, Accordion, ProgressBar, ListGroup } from 'react-bootstrap';
import { FiTarget, FiCheckCircle, FiAlertCircle, FiTrendingUp, FiBook } from 'react-icons/fi';
import { SkillGapAnalysis as SkillGapAnalysisType } from '../services/matchingService';

interface SkillGapAnalysisProps {
    analysis: SkillGapAnalysisType;
}

const SkillGapAnalysis: React.FC<SkillGapAnalysisProps> = ({ analysis }) => {
    const {
        matchedSkills,
        missingSkills,
        transferableSkills,
        recommendedLearningPath
    } = analysis;

    const getImportanceColor = (importance: string): string => {
        if (importance === 'HIGH') return 'danger';
        if (importance === 'MEDIUM') return 'warning';
        return 'info';
    };

    return (
        <Card className="border-0 shadow-sm mb-4">
            <Card.Body className="p-4">
                <h5 className="fw-bold mb-4">
                    <FiTarget className="me-2" />
                    Skill Gap Analysis
                </h5>

                {/* Matched Skills */}
                <div className="mb-4">
                    <h6 className="fw-semibold mb-3 text-success">
                        <FiCheckCircle className="me-2" />
                        Matched Skills ({matchedSkills.length})
                    </h6>
                    <div>
                        {matchedSkills.map((skill, index) => (
                            <Badge key={index} bg="success" className="me-2 mb-2 p-2">
                                {skill}
                            </Badge>
                        ))}
                    </div>
                </div>

                {/* Missing Skills */}
                {missingSkills.length > 0 && (
                    <div className="mb-4">
                        <h6 className="fw-semibold mb-3 text-warning">
                            <FiAlertCircle className="me-2" />
                            Skills to Develop ({missingSkills.length})
                        </h6>
                        <ListGroup variant="flush">
                            {missingSkills.map((skill, index) => (
                                <ListGroup.Item key={index} className="px-0 py-3">
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div className="flex-grow-1">
                                            <div className="d-flex align-items-center mb-2">
                                                <strong className="me-2">{skill.skillName}</strong>
                                                <Badge bg={getImportanceColor(skill.importance)}>
                                                    {skill.importance}
                                                </Badge>
                                            </div>
                                            {skill.transferableFrom && skill.transferableFrom.length > 0 && (
                                                <small className="text-muted">
                                                    Can leverage your experience in:{' '}
                                                    <strong>{skill.transferableFrom.join(', ')}</strong>
                                                </small>
                                            )}
                                        </div>
                                    </div>
                                </ListGroup.Item>
                            ))}
                        </ListGroup>
                    </div>
                )}

                {/* Transferable Skills */}
                {transferableSkills.length > 0 && (
                    <div className="mb-4">
                        <h6 className="fw-semibold mb-3 text-info">
                            <FiTrendingUp className="me-2" />
                            Transferable Skills
                        </h6>
                        <small className="text-muted d-block mb-3">
                            Your existing skills that are relevant to this role
                        </small>
                        <ListGroup variant="flush">
                            {transferableSkills.map((skill, index) => (
                                <ListGroup.Item key={index} className="px-0 py-2">
                                    <div className="d-flex justify-content-between align-items-center mb-1">
                                        <span>
                                            <strong>{skill.candidateSkill}</strong> → {skill.jobSkill}
                                        </span>
                                        <Badge bg="info">
                                            {(skill.transferabilityScore * 100).toFixed(0)}%
                                        </Badge>
                                    </div>
                                    <ProgressBar
                                        now={skill.transferabilityScore * 100}
                                        variant="info"
                                        style={{ height: '6px' }}
                                    />
                                </ListGroup.Item>
                            ))}
                        </ListGroup>
                    </div>
                )}

                {/* Recommended Learning Path */}
                {recommendedLearningPath.length > 0 && (
                    <div>
                        <h6 className="fw-semibold mb-3 text-primary">
                            <FiBook className="me-2" />
                            Recommended Learning Path
                        </h6>
                        <small className="text-muted d-block mb-3">
                            Personalized skill development roadmap based on your current experience
                        </small>
                        <Accordion>
                            {recommendedLearningPath.map((item, index) => (
                                <Accordion.Item key={index} eventKey={String(index)}>
                                    <Accordion.Header>
                                        <div className="d-flex justify-content-between w-100 pe-3">
                                            <strong>{item.skillName}</strong>
                                            <Badge bg="primary">
                                                {item.estimatedTimeWeeks} weeks
                                            </Badge>
                                        </div>
                                    </Accordion.Header>
                                    <Accordion.Body>
                                        {item.prerequisites.length > 0 && (
                                            <div className="mb-3">
                                                <small className="text-muted d-block mb-2">Prerequisites:</small>
                                                <div>
                                                    {item.prerequisites.map((prereq, i) => (
                                                        <Badge key={i} bg="secondary" className="me-1 mb-1">
                                                            {prereq}
                                                        </Badge>
                                                    ))}
                                                </div>
                                            </div>
                                        )}
                                        {item.resources.length > 0 && (
                                            <div>
                                                <small className="text-muted d-block mb-2">Recommended Resources:</small>
                                                <ul className="mb-0">
                                                    {item.resources.map((resource, i) => (
                                                        <li key={i}>
                                                            <small>{resource}</small>
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}
                                    </Accordion.Body>
                                </Accordion.Item>
                            ))}
                        </Accordion>
                    </div>
                )}

                {/* Summary */}
                <div className="mt-4 p-3 bg-light rounded">
                    <small className="text-muted">
                        <strong>Tip:</strong> Focus on high-priority missing skills first. Your existing experience in{' '}
                        {matchedSkills.slice(0, 3).join(', ')} gives you a strong foundation for this role.
                    </small>
                </div>
            </Card.Body>
        </Card>
    );
};

export default SkillGapAnalysis;
