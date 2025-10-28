import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Spinner, Container } from 'react-bootstrap';

interface PrivateRouteProps {
    children: React.ReactNode;
    requiredRole?: 'CANDIDATE' | 'RECRUITER';
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children, requiredRole }) => {
    const { isAuthenticated, user, loading } = useAuth();

    if (loading) {
        return (
            <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </Spinner>
            </Container>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (requiredRole && user?.role !== requiredRole) {
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children}</>;
};

export default PrivateRoute;
