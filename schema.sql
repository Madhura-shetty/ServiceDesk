-- ==========================================
-- ServiceDesk - PostgreSQL Setup Script
-- ==========================================
-- This script is OPTIONAL. If spring.jpa.hibernate.ddl-auto=update
-- is left in application.properties, Hibernate will create these
-- tables automatically on application startup.
--
-- Use this script only if you prefer to create the schema manually.

-- 1. Create the database (run this once, outside of any database, e.g. from the 'postgres' database)
-- CREATE DATABASE servicedesk_db;

-- Then connect to servicedesk_db and run the statements below.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL CHECK (role IN ('EMPLOYEE', 'SUPPORT_AGENT'))
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    resolution_deadline TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    assigned_agent BIGINT REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ticket_comments (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(2000) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    author_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_priority ON tickets(priority);
CREATE INDEX IF NOT EXISTS idx_comments_ticket ON ticket_comments(ticket_id);
