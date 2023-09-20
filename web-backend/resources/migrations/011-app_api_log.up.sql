CREATE TABLE app_api_log
-- app access log
(
	app_id INT8 PRIMARY KEY, 	-- app id
	access_time timestamp DEFAULT now(), 	-- 访问时间
	access_url varchar(100)    	-- 访问地址
);