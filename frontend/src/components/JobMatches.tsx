import React from 'react';
import { Card, ListGroup, Badge } from 'react-bootstrap';
import { Job } from '../types/Job';

interface JobMatchesProps {
    jobs: Job[];
}

const JobMatches: React.FC<JobMatchesProps> = ({ jobs }) => {
    return (
        <Card>
            <Card.Header as="h5">Top Job Matches</Card.Header>
            <ListGroup variant="flush">
                {jobs.map((job) => (
                    <ListGroup.Item key={job.id}>
                        <div className="d-flex justify-content-between">
                            <h6 className="mb-1">{job.title} - {job.company.name}</h6>
                            {job.matchScore && (
                                <Badge bg="success">{(job.matchScore * 100).toFixed(0)}% Match</Badge>
                            )}
                        </div>
                        <p className="mb-1">{job.company.location}</p>
                        <div>
                            {job.skills.slice(0, 5).map((skill, index) => (
                                <Badge key={index} pill bg="secondary" className="me-1">
                                    {skill}
                                </Badge>
                            ))}
                        </div>
                    </ListGroup.Item>
                ))}
            </ListGroup>
        </Card>
    );
};

export default JobMatches;
