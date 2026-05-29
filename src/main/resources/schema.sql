CREATE TABLE IF NOT EXISTS novel (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    folder_path VARCHAR(500) NOT NULL,
    create_time DATETIME NOT NULL
);
