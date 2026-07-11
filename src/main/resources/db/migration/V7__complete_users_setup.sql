-- Insert remaining users
INSERT INTO users (name, email) VALUES
('Riya', 'riya@example.com'),
('Kirti', 'kirti@example.com'),
('Sneha', 'sneha@example.com'),
('Neha', 'neha@example.com'),
('Ram', 'ram@example.com'),
('Ronak', 'ronak@example.com'),
('Tanish', 'tanish@example.com'),
('Priya', 'priya@example.com'),
('Harsh', 'harsh@example.com'),
('Amit', 'amit@example.com')
ON CONFLICT (email) DO NOTHING;

-- Add role column if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50);

-- Set roles
UPDATE users SET role = 'DONOR' 
WHERE name IN ('Alice','Bob','Riya','Kirti','Sneha','Neha','Charlie','David','Eve');

UPDATE users SET role = 'NGO' 
WHERE name IN ('Ram','Ronak');

UPDATE users SET role = 'PROVIDER' 
WHERE name IN ('Tanish','Priya');

UPDATE users SET role = 'ADMIN' 
WHERE name IN ('Harsh','Amit');