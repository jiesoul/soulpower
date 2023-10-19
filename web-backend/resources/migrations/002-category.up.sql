CREATE TABLE IF NOT EXISTS category
-- 分类
(
    id serial PRIMARY KEY,  -- 分类ID
    name VARCHAR(20) unique,    -- 分类名称
    description TEXT,    -- 分类描述
    pid INTEGER DEFAULT 0   -- 父分类ID
);
