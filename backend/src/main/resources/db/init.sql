-- 创建数据库
CREATE DATABASE IF NOT EXISTS community_library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE community_library;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('READER', 'ADMIN') NOT NULL DEFAULT 'READER',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 图书分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20),
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(100),
    publish_date DATE,
    category_id BIGINT,
    description TEXT,
    cover_image VARCHAR(500),
    total_quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1,
    location VARCHAR(100),
    status ENUM('AVAILABLE', 'BORROWED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_category_id (category_id),
    INDEX idx_isbn (isbn),
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('PENDING', 'APPROVED', 'BORROWED', 'RETURNED', 'REJECTED', 'OVERDUE') NOT NULL DEFAULT 'PENDING',
    admin_id BIGINT,
    reject_reason VARCHAR(500),
    fine_amount DECIMAL(10,2) DEFAULT 0.00,
    fine_paid BOOLEAN DEFAULT FALSE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_borrow_date (borrow_date),
    INDEX idx_due_date (due_date),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 统计数据表（用于统计功能）
CREATE TABLE IF NOT EXISTS statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL UNIQUE,
    total_users INT DEFAULT 0,
    total_books INT DEFAULT 0,
    total_borrowed INT DEFAULT 0,
    total_returned INT DEFAULT 0,
    total_overdue INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入初始分类数据
INSERT INTO categories (name, description) VALUES
('文学小说', '各类文学作品、小说、散文等'),
('科技编程', '计算机科学、编程技术、人工智能等'),
('历史传记', '历史书籍、人物传记等'),
('经济管理', '经济学、管理学、商业等'),
('教育学习', '教育理论、学习方法等'),
('艺术设计', '艺术、设计、音乐等'),
('生活休闲', '生活方式、休闲娱乐等'),
('科学技术', '自然科学、工程技术等');

-- 插入初始管理员账户（密码: admin123，使用BCrypt加密）
INSERT INTO users (username, password, real_name, email, phone, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@library.com', '13800138000', 'ADMIN', 'ACTIVE');

-- 插入测试用户（密码: user123）
INSERT INTO users (username, password, real_name, email, phone, role, status) VALUES
('reader1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '张三', 'zhangsan@example.com', '13800138001', 'READER', 'ACTIVE'),
('reader2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '李四', 'lisi@example.com', '13800138002', 'READER', 'ACTIVE');

-- 插入测试图书数据
INSERT INTO books (isbn, title, author, publisher, publish_date, category_id, description, total_quantity, available_quantity, location, status) VALUES
('978-7-115-42818-6', 'Java核心技术卷I', 'Cay S. Horstmann', '人民邮电出版社', '2019-05-01', 2, '本书是Java领域最有影响力和价值的著作之一，拥有20多年的历史，被誉为Java四大名著之首。', 10, 10, 'A区-01架', 'AVAILABLE'),
('978-7-111-21382-6', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '2016-11-01', 2, '这本书从程序员的视角详细阐述计算机系统的本质概念。', 5, 5, 'A区-01架', 'AVAILABLE'),
('978-7-5442-7087-9', '活着', '余华', '作家出版社', '2012-08-01', 1, '《活着》讲述了农村人福贵悲惨的人生遭遇。', 20, 18, 'B区-01架', 'AVAILABLE'),
('978-7-5404-4888-9', '明朝那些事儿', '当年明月', '浙江人民出版社', '2017-05-01', 3, '以史料为基础，以年代和具体人物为主线，对明朝十七帝和其他王公权贵和小人物的命运进行全景展示。', 15, 15, 'C区-01架', 'AVAILABLE'),
('978-7-5086-6093-1', '人类简史', '尤瓦尔·赫拉利', '中信出版社', '2017-02-01', 3, '十万年前，地球上至少有六种不同的人，但今日，世界舞台为什么只剩下了我们自己？', 8, 7, 'C区-02架', 'AVAILABLE'),
('978-7-111-59334-0', '算法导论', 'Thomas H. Cormen', '机械工业出版社', '2018-09-01', 2, '本书提供了对算法的全面介绍，旨在全面讨论算法的设计与分析。', 6, 6, 'A区-02架', 'AVAILABLE'),
('978-7-5594-0086-6', '解忧杂货店', '东野圭吾', '南海出版公司', '2014-05-01', 1, '僻静的街道旁有一家特别的杂货店，只要写下烦恼投进店前门卷帘门的投信口，第二天就会在店后的牛奶箱里得到回答。', 12, 11, 'B区-02架', 'AVAILABLE'),
('978-7-111-40701-0', '设计模式', 'Erich Gamma', '机械工业出版社', '2019-10-01', 2, '设计模式是软件工程的基石，每一个模式描述了一个在我们周围不断重复发生的问题，以及该问题的解决方案的核心。', 8, 8, 'A区-03架', 'AVAILABLE');
