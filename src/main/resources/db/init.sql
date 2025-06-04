-- 创建数据库
CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始管理员用户（密码：admin123）
INSERT INTO users (username, password, name, email, role, status, created_at, updated_at)
VALUES ('admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', '系统管理员', 'admin@example.com', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- 插入测试用户（密码：test123）
INSERT INTO users (username, password, name, email, phone, role, status, created_at, updated_at)
VALUES ('test', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', '测试用户', 'test@example.com', '13800138000', 'READER', 'ACTIVE', NOW(), NOW());

-- 创建图书分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    description TEXT,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入图书分类
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('计算机科学', '计算机相关书籍', NOW(), NOW()),
('文学', '文学类书籍', NOW(), NOW()),
('历史', '历史类书籍', NOW(), NOW()),
('艺术', '艺术类书籍', NOW(), NOW()),
('科学', '科学类书籍', NOW(), NOW());

-- 插入示例图书
INSERT INTO books (title, author, isbn, category_id, description, total_copies, available_copies, status, created_at, updated_at) VALUES
('Java编程思想', 'Bruce Eckel', '9787111213826', 1, 'Java编程经典著作', 5, 5, 'AVAILABLE', NOW(), NOW()),
('算法导论', 'Thomas H. Cormen', '9787111187776', 1, '计算机算法经典教材', 3, 3, 'AVAILABLE', NOW(), NOW()),
('红楼梦', '曹雪芹', '9787020002207', 2, '中国古典四大名著之一', 10, 10, 'AVAILABLE', NOW(), NOW()),
('史记', '司马迁', '9787101003048', 3, '中国第一部纪传体通史', 8, 8, 'AVAILABLE', NOW(), NOW()),
('艺术的故事', '贡布里希', '9787807463581', 4, '艺术史经典著作', 4, 4, 'AVAILABLE', NOW(), NOW()),
('时间简史', '史蒂芬·霍金', '9787535732309', 5, '探索宇宙奥秘的科普著作', 6, 6, 'AVAILABLE', NOW(), NOW()); 