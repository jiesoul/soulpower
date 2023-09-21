CREATE TABLE app_access_log
-- app access log
(
	id serial PRIMARY KEY
	app_id varchar(32), 	-- app id
	access_time timestamp DEFAULT now(), 	-- 访问时间
	access_url varchar(100)    	-- 访问地址
);