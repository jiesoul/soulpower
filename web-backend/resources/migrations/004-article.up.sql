CREATE TABLE article
-- 文章
(
    id VARCHAR(20) PRIMARY KEY NOT NULL,    -- 博文ID
    author VARCHAR(32),                 -- 发表用户
    title VARCHAR(1024),                -- 博文标题
    create_time timestamp DEFAULT now(),               -- 创建时间
    like_count INT DEFAULT 0,           -- 点赞数
    comment_count INT DEFAULT 0,        -- 评论数
    read_count INT DEFAULT 0,           -- 浏览量
    top_flag INT DEFAULT 0,               -- 是否置顶
    category_id INT DEFAULT 0,           -- 分类
    push_time timestamp,                 -- 发布时间
    push_flag INT DEFAULT 0,
    summary VARCHAR(1024) 
);