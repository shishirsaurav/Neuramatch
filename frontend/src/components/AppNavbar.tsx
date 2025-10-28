import React from 'react';
import { Navbar, Container, Nav, NavDropdown } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FiUser, FiLogOut, FiSettings } from 'react-icons/fi';

const AppNavbar = () => {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const isCandidate = user?.role === 'CANDIDATE';
    const isRecruiter = user?.role === 'RECRUITER';

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <Navbar bg="dark" variant="dark" expand="lg" className="shadow-sm">
            <Container>
                <Navbar.Brand as={Link} to="/" className="fw-bold fs-4">
                    NeuraMatch
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    {isAuthenticated ? (
                        <>
                            <Nav className="me-auto">
                                <Nav.Link as={Link} to="/dashboard">Dashboard</Nav.Link>

                                {isCandidate && (
                                    <>
                                        <Nav.Link as={Link} to="/jobs">Browse Jobs</Nav.Link>
                                        <Nav.Link as={Link} to="/upload-resume">Upload Resume</Nav.Link>
                                        <Nav.Link as={Link} to="/analytics">Insights</Nav.Link>
                                    </>
                                )}

                                {isRecruiter && (
                                    <>
                                        <Nav.Link as={Link} to="/recruiter/post-job">Post Job</Nav.Link>
                                        <Nav.Link as={Link} to="/recruiter/my-jobs">My Jobs</Nav.Link>
                                        <Nav.Link as={Link} to="/recruiter/candidates">Candidates</Nav.Link>
                                        <Nav.Link as={Link} to="/analytics">Analytics</Nav.Link>
                                    </>
                                )}
                            </Nav>

                            <Nav>
                                <NavDropdown
                                    title={
                                        <span>
                                            <FiUser className="me-2" />
                                            {user?.firstName} {user?.lastName}
                                        </span>
                                    }
                                    id="user-dropdown"
                                    align="end"
                                >
                                    <NavDropdown.Item as={Link} to="/profile">
                                        <FiUser className="me-2" />
                                        Profile
                                    </NavDropdown.Item>
                                    <NavDropdown.Item as={Link} to="/settings">
                                        <FiSettings className="me-2" />
                                        Settings
                                    </NavDropdown.Item>
                                    <NavDropdown.Divider />
                                    <NavDropdown.Item onClick={handleLogout}>
                                        <FiLogOut className="me-2" />
                                        Logout
                                    </NavDropdown.Item>
                                </NavDropdown>
                            </Nav>
                        </>
                    ) : (
                        <Nav className="ms-auto">
                            <Nav.Link as={Link} to="/login">Login</Nav.Link>
                            <Nav.Link as={Link} to="/register">Sign Up</Nav.Link>
                        </Nav>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default AppNavbar;
