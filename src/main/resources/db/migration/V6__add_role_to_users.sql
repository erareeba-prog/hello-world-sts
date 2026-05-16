-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(50);

-- Set default roles for existing users
UPDATE users SET role = 'DONOR' WHERE name IN ('Alice', 'Bob', 'Riya', 'Kirti', 'Sneha', 'Neha');
UPDATE users SET role = 'NGO' WHERE name IN ('Ram', 'Ronak');
UPDATE users SET role = 'PROVIDER' WHERE name IN ('Tanish', 'Priya');
UPDATE users SET role = 'ADMIN' WHERE name IN ('Harsh', 'Amit');