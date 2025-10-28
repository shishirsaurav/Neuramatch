import React, { useState } from 'react';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiMail, FiLock } from 'react-icons/fi';

const Login: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login({ email, password });
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.response?.data?.error || err.response?.data?.message || 'Failed to login. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh' }}>
            <div style={{ width: '100%', maxWidth: '450px' }}>
                <Card className="shadow-lg border-0">
                    <Card.Body className="p-5">
                        <div className="text-center mb-4">
                            <h2 className="fw-bold mb-2">Welcome Back</h2>
                            <p className="text-muted">Sign in to your NeuraMatch account</p>
                        </div>

                        {error && <Alert variant="danger">{error}</Alert>}

                        <Form onSubmit={handleSubmit}>
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

                            <Form.Group className="mb-4">
                                <Form.Label className="fw-semibold">
                                    <FiLock className="me-2" />
                                    Password
                                </Form.Label>
                                <Form.Control
                                    type="password"
                                    placeholder="Enter your password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    size="lg"
                                />
                            </Form.Group>

                            <Button
                                variant="primary"
                                type="submit"
                                size="lg"
                                className="w-100 mb-3"
                                disabled={loading}
                            >
                                {loading ? 'Signing in...' : 'Sign In'}
                            </Button>

                            <div className="text-center">
                                <p className="mb-0">
                                    Don't have an account?{' '}
                                    <Link to="/register" className="text-decoration-none fw-semibold">
                                        Sign up
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

export default Login;
