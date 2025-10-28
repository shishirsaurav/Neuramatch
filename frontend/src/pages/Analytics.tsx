import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Spinner, Alert } from 'react-bootstrap';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { FiTrendingUp, FiDollarSign, FiUsers, FiTarget } from 'react-icons/fi';
import analyticsService, { TrendingSkill, SalaryBenchmark, HiringFunnelMetrics } from '../services/analyticsService';

const Analytics: React.FC = () => {
    const [trendingSkills, setTrendingSkills] = useState<TrendingSkill[]>([]);
    const [salaryBenchmarks, setSalaryBenchmarks] = useState<SalaryBenchmark[]>([]);
    const [hiringFunnel, setHiringFunnel] = useState<HiringFunnelMetrics | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadAnalytics();
    }, []);

    const loadAnalytics = async () => {
        setLoading(true);
        setError('');

        try {
            const [skills, funnel] = await Promise.all([
                analyticsService.getTrendingSkills(undefined, 10),
                analyticsService.getHiringFunnelMetrics()
            ]);

            setTrendingSkills(skills);
            setHiringFunnel(funnel);

            // Load salary benchmarks for top skills
            if (skills.length > 0) {
                const topSkills = skills.slice(0, 8).map(s => s.skillName);
                const benchmarks = await analyticsService.getSalaryBenchmarks(topSkills);
                setSalaryBenchmarks(benchmarks);
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load analytics');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Container className="py-5 text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </Spinner>
            </Container>
        );
    }

    if (error) {
        return (
            <Container className="py-5">
                <Alert variant="danger">{error}</Alert>
            </Container>
        );
    }

    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D', '#FFC658', '#FF6B9D'];

    const funnelChartData = hiringFunnel ? [
        { name: 'Applications', value: hiringFunnel.totalApplications },
        { name: 'Shortlisted', value: hiringFunnel.shortlisted },
        { name: 'Interviewed', value: hiringFunnel.interviewed },
        { name: 'Offered', value: hiringFunnel.offered },
        { name: 'Hired', value: hiringFunnel.hired },
    ] : [];

    return (
        <Container className="py-4">
            <Row className="mb-4">
                <Col>
                    <h2 className="fw-bold mb-2">Analytics & Insights</h2>
                    <p className="text-muted">Market trends, salary benchmarks, and hiring metrics</p>
                </Col>
            </Row>

            {/* Key Metrics */}
            {hiringFunnel && (
                <Row className="g-4 mb-4">
                    <Col md={3}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body>
                                <div className="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p className="text-muted mb-1">Total Applications</p>
                                        <h3 className="fw-bold mb-0">{hiringFunnel.totalApplications}</h3>
                                    </div>
                                    <div className="bg-primary bg-opacity-10 p-3 rounded">
                                        <FiUsers size={24} className="text-primary" />
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col md={3}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body>
                                <div className="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p className="text-muted mb-1">Hired</p>
                                        <h3 className="fw-bold mb-0">{hiringFunnel.hired}</h3>
                                    </div>
                                    <div className="bg-success bg-opacity-10 p-3 rounded">
                                        <FiTarget size={24} className="text-success" />
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col md={3}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body>
                                <div className="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p className="text-muted mb-1">Avg. Time to Hire</p>
                                        <h3 className="fw-bold mb-0">{hiringFunnel.avgTimeToHire} days</h3>
                                    </div>
                                    <div className="bg-warning bg-opacity-10 p-3 rounded">
                                        <FiTrendingUp size={24} className="text-warning" />
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col md={3}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body>
                                <div className="d-flex justify-content-between align-items-center">
                                    <div>
                                        <p className="text-muted mb-1">Offer to Hire</p>
                                        <h3 className="fw-bold mb-0">
                                            {(hiringFunnel.conversionRates.offerToHire * 100).toFixed(1)}%
                                        </h3>
                                    </div>
                                    <div className="bg-info bg-opacity-10 p-3 rounded">
                                        <FiDollarSign size={24} className="text-info" />
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            )}

            <Row className="g-4 mb-4">
                {/* Trending Skills */}
                <Col md={6}>
                    <Card className="border-0 shadow-sm h-100">
                        <Card.Body className="p-4">
                            <h5 className="fw-bold mb-4">
                                <FiTrendingUp className="me-2" />
                                Trending Skills
                            </h5>
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={trendingSkills}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="skillName" angle={-45} textAnchor="end" height={100} />
                                    <YAxis />
                                    <Tooltip />
                                    <Bar dataKey="trendScore" fill="#0088FE" name="Trend Score" />
                                    <Bar dataKey="demandCount" fill="#00C49F" name="Demand" />
                                </BarChart>
                            </ResponsiveContainer>
                        </Card.Body>
                    </Card>
                </Col>

                {/* Salary Benchmarks */}
                <Col md={6}>
                    <Card className="border-0 shadow-sm h-100">
                        <Card.Body className="p-4">
                            <h5 className="fw-bold mb-4">
                                <FiDollarSign className="me-2" />
                                Salary Benchmarks
                            </h5>
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={salaryBenchmarks}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="skillName" angle={-45} textAnchor="end" height={100} />
                                    <YAxis />
                                    <Tooltip formatter={(value) => `$${value.toLocaleString()}`} />
                                    <Bar dataKey="avgSalary" fill="#8884D8" name="Avg Salary" />
                                </BarChart>
                            </ResponsiveContainer>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>

            <Row className="g-4">
                {/* Hiring Funnel */}
                {hiringFunnel && (
                    <Col md={6}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body className="p-4">
                                <h5 className="fw-bold mb-4">
                                    <FiUsers className="me-2" />
                                    Hiring Funnel
                                </h5>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={funnelChartData} layout="horizontal">
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis type="number" />
                                        <YAxis type="category" dataKey="name" />
                                        <Tooltip />
                                        <Bar dataKey="value" fill="#0088FE">
                                            {funnelChartData.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </Card.Body>
                        </Card>
                    </Col>
                )}

                {/* Conversion Rates */}
                {hiringFunnel && (
                    <Col md={6}>
                        <Card className="border-0 shadow-sm h-100">
                            <Card.Body className="p-4">
                                <h5 className="fw-bold mb-4">
                                    <FiTarget className="me-2" />
                                    Conversion Rates
                                </h5>
                                <ResponsiveContainer width="100%" height={300}>
                                    <PieChart>
                                        <Pie
                                            data={[
                                                { name: 'App → Shortlist', value: hiringFunnel.conversionRates.applicationToShortlist },
                                                { name: 'Shortlist → Interview', value: hiringFunnel.conversionRates.shortlistToInterview },
                                                { name: 'Interview → Offer', value: hiringFunnel.conversionRates.interviewToOffer },
                                                { name: 'Offer → Hire', value: hiringFunnel.conversionRates.offerToHire },
                                            ]}
                                            cx="50%"
                                            cy="50%"
                                            labelLine={false}
                                            label={(entry: any) => `${(entry.value * 100).toFixed(1)}%`}
                                            outerRadius={80}
                                            fill="#8884d8"
                                            dataKey="value"
                                        >
                                            {funnelChartData.map((entry, index) => (
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                            ))}
                                        </Pie>
                                        <Tooltip formatter={(value: number) => `${(value * 100).toFixed(1)}%`} />
                                        <Legend />
                                    </PieChart>
                                </ResponsiveContainer>
                            </Card.Body>
                        </Card>
                    </Col>
                )}
            </Row>
        </Container>
    );
};

export default Analytics;
