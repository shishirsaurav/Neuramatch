import React, { useState } from 'react';
import { Container, Card, Form, Button, Alert, ButtonGroup } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiMail, FiLock, FiUser, FiBriefcase } from 'react-icons/fi';

const Register: React.FC = () => {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [role, setRole] = useState<'CANDIDATE' | 'RECRUITER'>('CANDIDATE');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (password.length < 8) {
            setError('Password must be at least 8 characters long');
            return;
        }

        setLoading(true);

        try {
            await register({ email, password, firstName, lastName, role });
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.response?.data?.error || err.response?.data?.message || 'Failed to register. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh', paddingTop: '2rem', paddingBottom: '2rem' }}>
            <div style={{ width: '100%', maxWidth: '500px' }}>
                <Card className="shadow-lg border-0">
                    <Card.Body className="p-5">
                        <div className="text-center mb-4">
                            <h2 className="fw-bold mb-2">Create Account</h2>
                            <p className="text-muted">Join NeuraMatch today</p>
                        </div>

                        {error && <Alert variant="danger">{error}</Alert>}

                        <Form onSubmit={handleSubmit}>
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">
                                    <FiUser className="me-2" />
                                    First Name
                                </Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Enter your first name"
                                    value={firstName}
                                    onChange={(e) => setFirstName(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">
                                    <FiUser className="me-2" />
                                    Last Name
                                </Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Enter your last name"
                                    value={lastName}
                                    onChange={(e) => setLastName(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">
                                    <FiMail className="me-2" />
                                    Email Address
                                </Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter your email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">
                                    <FiLock className="me-2" />
                                    Password
                                </Form.Label>
                                <Form.Control
                                    type="password"
                                    placeholder="Create a password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">
                                    <FiLock className="me-2" />
                                    Confirm Password
                                </Form.Label>
                                <Form.Control
                                    type="password"
                                    placeholder="Confirm your password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Form.Group className="mb-4">
                                <Form.Label className="fw-semibold">
                                    <FiBriefcase className="me-2" />
                                    I am a...
                                </Form.Label>
                                <div className="d-grid gap-2">
                                    <ButtonGroup>
                                        <Button
                                            variant={role === 'CANDIDATE' ? 'primary' : 'outline-primary'}
                                            onClick={() => setRole('CANDIDATE')}
                                            size="lg"
                                        >
                                            Job Seeker
                                        </Button>
                                        <Button
                                            variant={role === 'RECRUITER' ? 'primary' : 'outline-primary'}
                                            onClick={() => setRole('RECRUITER')}
                                            size="lg"
                                        >
                                            Recruiter
                                        </Button>
                                    </ButtonGroup>
                                </div>
                            </Form.Group>

                            <Button
                                variant="primary"
                                type="submit"
                                size="lg"
                                className="w-100 mb-3"
                                disabled={loading}
                            >
                                {loading ? 'Creating Account...' : 'Create Account'}
                            </Button>

                            <div className="text-center">
                                <p className="mb-0">
                                    Already have an account?{' '}
                                    <Link to="/login" className="text-decoration-none fw-semibold">
                                        Sign in
                                    </Link>
                                </p>
                            </div>
                        </Form>
                    </Card.Body>
                </Card>

                <div className="text-center mt-4 text-muted">
                    <small>&copy; 2025 NeuraMatch. All rights reserved.</small>
                </div>
            </div>
        </Container>
    );
};

export default Register;
