-- Initialize PostgreSQL with pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create schemas
CREATE SCHEMA IF NOT EXISTS neuramatch;

-- Set search path
SET search_path TO neuramatch, public;

-- Create initial tables will be handled by JPA/Hibernate
-- This file is for initial database setup and extensions

SELECT 'NeuraMatch database initialized successfully!' AS status;
