-- Migration script to update vector dimensions from 3072 (OpenAI) to 768 (Gemini)
-- Run this if you're switching from OpenAI to Gemini embeddings

-- Drop existing indexes
DROP INDEX IF EXISTS idx_resume_vector_embedding;
DROP INDEX IF EXISTS idx_job_vector_embedding;

-- Alter column definitions
ALTER TABLE resume_vectors
ALTER COLUMN embedding TYPE vector(768);

ALTER TABLE job_vectors
ALTER COLUMN embedding TYPE vector(768);

-- Recreate HNSW indexes for faster similarity search
CREATE INDEX idx_resume_vector_embedding ON resume_vectors
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

CREATE INDEX idx_job_vector_embedding ON job_vectors
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- Clear all existing embeddings (they need to be regenerated with Gemini)
TRUNCATE TABLE resume_vectors;
TRUNCATE TABLE job_vectors;

SELECT 'Migration to Gemini (768 dimensions) complete. Please regenerate all embeddings.' AS status;
