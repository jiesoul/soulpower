CREATE TABLE app
-- app auth
(
	id varchar(32) PRIMARY KEY,
	name varchar(100) NOT NULL, -- 应用名称
	secret varchar(32) NOT NULL, -- App 密钥
	app_category_id varchar(16) NOT NULL, -- 分类
    description varchar(200) -- App 简介

);