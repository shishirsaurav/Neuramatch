import React from 'react';
import { Card, Row, Col, Badge } from 'react-bootstrap';
import { Resume } from '../types/Resume';

interface UserProfileProps {
    resume: Resume;
}

const UserProfile: React.FC<UserProfileProps> = ({ resume }) => {
    return (
        <Card>
            <Card.Header>
                <Card.Title className="mb-0">{resume.name}</Card.Title>
                <Card.Subtitle className="text-muted">{resume.email} | {resume.phone}</Card.Subtitle>
            </Card.Header>
            <Card.Body>
                <Row>
                    <Col md={12}>
                        <h5>Summary</h5>
                        <p>{resume.summary}</p>
                    </Col>
                </Row>
                <hr />
                <Row>
                    <Col md={12}>
                        <h5>Skills</h5>
                        <div>
                            {resume.skills.map((skill, index) => (
                                <Badge key={index} pill bg="primary" className="me-1 mb-1">
                                    {skill.name}
                                </Badge>
                            ))}
                        </div>
                    </Col>
                </Row>
                <hr />
                <Row>
                    <Col md={12}>
                        <h5>Work Experience</h5>
                        {resume.experience.map((exp, index) => (
                            <div key={index} className="mb-3">
                                <h6>{exp.jobTitle} at {exp.company}</h6>
                                <small className="text-muted">{exp.startDate} - {exp.endDate || 'Present'} | {exp.location}</small>
                                <p>{exp.description}</p>
                            </div>
                        ))}
                    </Col>
                </Row>
                <hr />
                <Row>
                    <Col md={12}>
                        <h5>Education</h5>
                        {resume.education.map((edu, index) => (
                            <div key={index} className="mb-2">
                                <h6>{edu.institution}</h6>
                                <p className="mb-0">{edu.degree}, {edu.fieldOfStudy}</p>
                                <small className="text-muted">{edu.startDate} - {edu.endDate || 'Present'}</small>
                            </div>
                        ))}
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
};

export default UserProfile;
