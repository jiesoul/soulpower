CREATE TABLE app_api 
-- app auth
(
	id serial PRIMARY KEY,
	app_name varchar(100) NOT NULL, -- 应用名称
	app_key varchar(16) NOT NULL, -- App Key
	app_secret varchar(32) NOT NULL, -- App 密钥
    app_description varchar(200) -- App 简介
);