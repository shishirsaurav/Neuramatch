import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, ProgressBar, Alert, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { FiUpload, FiCheckCircle, FiAlertCircle } from 'react-icons/fi';
import resumeService from '../services/resumeService';

const UploadResume: React.FC = () => {
    const navigate = useNavigate();
    const [file, setFile] = useState<File | null>(null);
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [qualityScore, setQualityScore] = useState<number | null>(null);
    const [suggestions, setSuggestions] = useState<string[]>([]);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        if (event.target.files && event.target.files[0]) {
            const selectedFile = event.target.files[0];

            // Validate file size (5MB max)
            if (selectedFile.size > 5 * 1024 * 1024) {
                setError('File size must be less than 5MB');
                return;
            }

            // Validate file type
            const validTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
            if (!validTypes.includes(selectedFile.type)) {
                setError('Please upload a PDF, DOC, DOCX, or TXT file');
                return;
            }

            setFile(selectedFile);
            setError(null);
            setSuccess(false);
        }
    };

    const handleUpload = async () => {
        if (!file) return;

        setUploading(true);
        setUploadProgress(0);
        setError(null);
        setSuccess(false);

        try {
            const response = await resumeService.uploadResume(file, setUploadProgress);

            setSuccess(true);
            setQualityScore(response.qualityScore || null);
            setSuggestions(response.qualitySuggestions || []);

            // Redirect to job matches after 2 seconds
            setTimeout(() => {
                navigate('/jobs');
            }, 2000);
        } catch (err: any) {
            console.error('Upload error:', err);
            const errorMessage = err.response?.data?.error ||
                               err.response?.data?.message ||
                               err.message ||
                               'An unexpected error occurred';
            setError(errorMessage);
        } finally {
            setUploading(false);
        }
    };

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
    };

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            const droppedFile = e.dataTransfer.files[0];
            setFile(droppedFile);
            setError(null);
        }
    };

    return (
        <Container className="py-5">
            <Row className="justify-content-center">
                <Col md={8} lg={6}>
                    <div className="text-center mb-4">
                        <h2 className="fw-bold mb-3">Upload Your Resume</h2>
                        <p className="text-muted">
                            Our AI will analyze your resume and find the best job matches for you
                        </p>
                    </div>

                    <Card className="border-0 shadow-sm">
                        <Card.Body className="p-4 p-sm-5">
                            {success && (
                                <Alert variant="success" className="mb-4">
                                    <FiCheckCircle className="me-2" />
                                    Resume uploaded successfully! Finding your matches...
                                </Alert>
                            )}

                            {error && (
                                <Alert variant="danger" className="mb-4">
                                    <FiAlertCircle className="me-2" />
                                    {error}
                                </Alert>
                            )}

                            {qualityScore !== null && (
                                <Card className="bg-light border-0 mb-4">
                                    <Card.Body>
                                        <h6 className="fw-bold mb-3">Resume Quality Score</h6>
                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                            <span>Overall Quality</span>
                                            <Badge bg={qualityScore >= 70 ? 'success' : qualityScore >= 50 ? 'warning' : 'danger'}>
                                                {qualityScore}/100
                                            </Badge>
                                        </div>
                                        <ProgressBar
                                            now={qualityScore}
                                            variant={qualityScore >= 70 ? 'success' : qualityScore >= 50 ? 'warning' : 'danger'}
                                            className="mb-3"
                                        />
                                        {suggestions.length > 0 && (
                                            <>
                                                <h6 className="fw-semibold small mb-2">Suggestions:</h6>
                                                <ul className="small mb-0">
                                                    {suggestions.map((s, i) => (
                                                        <li key={i}>{s}</li>
                                                    ))}
                                                </ul>
                                            </>
                                        )}
                                    </Card.Body>
                                </Card>
                            )}

                            <div
                                className="border border-2 border-dashed rounded p-5 text-center mb-4"
                                onDragOver={handleDragOver}
                                onDrop={handleDrop}
                                style={{ cursor: 'pointer' }}
                                onClick={() => document.getElementById('file-input')?.click()}
                            >
                                <FiUpload size={48} className="text-muted mb-3" />
                                <h6 className="mb-2">Drag and drop your resume here</h6>
                                <p className="text-muted small mb-0">or click to browse</p>
                                <p className="text-muted small">
                                    Supported formats: PDF, DOC, DOCX, TXT (Max 5MB)
                                </p>
                            </div>

                            <Form.Control
                                id="file-input"
                                type="file"
                                accept=".pdf,.doc,.docx,.txt"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />

                            {file && (
                                <div className="mb-3">
                                    <small className="text-muted">Selected file:</small>
                                    <div className="d-flex justify-content-between align-items-center">
                                        <strong>{file.name}</strong>
                                        <Badge bg="secondary">{(file.size / 1024).toFixed(1)} KB</Badge>
                                    </div>
                                </div>
                            )}

                            {uploading && (
                                <div className="mb-3">
                                    <div className="d-flex justify-content-between mb-2">
                                        <small>Uploading...</small>
                                        <small>{uploadProgress}%</small>
                                    </div>
                                    <ProgressBar animated now={uploadProgress} />
                                </div>
                            )}

                            <Button
                                variant="primary"
                                size="lg"
                                className="w-100"
                                onClick={handleUpload}
                                disabled={!file || uploading}
                            >
                                {uploading ? 'Analyzing...' : 'Upload & Find Matches'}
                            </Button>

                            <div className="mt-4 p-3 bg-light rounded">
                                <small className="text-muted">
                                    <strong>Privacy Notice:</strong> Your resume is analyzed securely and confidentially.
                                    We use AI to extract relevant information and never share your data without permission.
                                </small>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default UploadResume;
