ALTER TABLE users ADD COLUMN password VARCHAR(255);

UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE name IN ('Riya','Kirti','Sneha','Neha','Ram','Ronak','Tanish','Priya','Harsh','Amit','Alice','Bob','Charlie','David','Eve');