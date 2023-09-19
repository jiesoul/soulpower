CREATE TABLE article_comment
-- 评论
(
    id serial PRIMARY KEY,  -- 评论ID
    create_time timestamp default now(),    -- 评论日期
    like_count INT default 0,    -- 点赞数
    username TEXT not null,    -- 发表用户
    user_email TEXT not null,  -- 用户邮箱
    article_id VARCHAR(32) not null,    -- 评论文章ID
    content TEXT not null,    -- 评论内容
    qid BIGINT default 0   -- 引用评论ID 
);