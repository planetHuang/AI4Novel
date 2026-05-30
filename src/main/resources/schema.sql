CREATE DATABASE IF NOT EXISTS ai4novel DEFAULT CHARSET utf8mb4;

USE ai4novel;

CREATE TABLE IF NOT EXISTS novel (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    folder_path VARCHAR(500) NOT NULL,
    create_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS ai_config (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(200) NOT NULL,
    model VARCHAR(100) NOT NULL DEFAULT '',
    thinking_enabled BOOLEAN DEFAULT FALSE,
    reasoning_effort VARCHAR(20) DEFAULT '',
    is_default BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_conversation (
    id VARCHAR(32) PRIMARY KEY,
    novel_id VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL DEFAULT '新对话',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(32) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL
);
