import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import { AuthProvider } from './contexts/AuthContext';
import AppNavbar from './components/AppNavbar';
import PrivateRoute from './components/PrivateRoute';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import UploadResume from './pages/UploadResume';
import JobSearch from './pages/JobSearch';
import JobDetails from './pages/JobDetails';
import Analytics from './pages/Analytics';

// Recruiter Pages
import PostJob from './pages/recruiter/PostJob';
import MyJobs from './pages/recruiter/MyJobs';
import Candidates from './pages/recruiter/Candidates';

// Placeholder components
const ProfilePage = () => <div className="container py-5"><h3>Profile Page - Coming Soon</h3></div>;
const SettingsPage = () => <div className="container py-5"><h3>Settings Page - Coming Soon</h3></div>;

function App() {
    return (
        <Router>
            <AuthProvider>
                <div className="App">
                    <AppNavbar />
                    <Routes>
                        {/* Public Routes */}
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />

                        {/* Protected Routes */}
                        <Route
                            path="/dashboard"
                            element={
                                <PrivateRoute>
                                    <Dashboard />
                                </PrivateRoute>
                            }
                        />

                        {/* Candidate Routes */}
                        <Route
                            path="/upload-resume"
                            element={
                                <PrivateRoute requiredRole="CANDIDATE">
                                    <UploadResume />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/jobs"
                            element={
                                <PrivateRoute requiredRole="CANDIDATE">
                                    <JobSearch />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/jobs/:id"
                            element={
                                <PrivateRoute requiredRole="CANDIDATE">
                                    <JobDetails />
                                </PrivateRoute>
                            }
                        />

                        {/* Recruiter Routes */}
                        <Route
                            path="/recruiter/post-job"
                            element={
                                <PrivateRoute requiredRole="RECRUITER">
                                    <PostJob />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/recruiter/my-jobs"
                            element={
                                <PrivateRoute requiredRole="RECRUITER">
                                    <MyJobs />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/recruiter/candidates"
                            element={
                                <PrivateRoute requiredRole="RECRUITER">
                                    <Candidates />
                                </PrivateRoute>
                            }
                        />

                        {/* Shared Routes */}
                        <Route
                            path="/analytics"
                            element={
                                <PrivateRoute>
                                    <Analytics />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/profile"
                            element={
                                <PrivateRoute>
                                    <ProfilePage />
                                </PrivateRoute>
                            }
                        />
                        <Route
                            path="/settings"
                            element={
                                <PrivateRoute>
                                    <SettingsPage />
                                </PrivateRoute>
                            }
                        />

                        {/* Default Route */}
                        <Route path="/" element={<Navigate to="/dashboard" replace />} />
                        <Route path="*" element={<Navigate to="/dashboard" replace />} />
                    </Routes>
                </div>
            </AuthProvider>
        </Router>
    );
}

export default App;
