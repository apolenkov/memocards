-- Rename news.author_email -> news.author to align with entity field
-- Works on H2 and PostgreSQL
ALTER TABLE news RENAME COLUMN author_email TO author;

