CREATE TABLE app_category
-- app auth
(
	id varchar(32) PRIMARY KEY,
	name varchar(100) NOT NULL, 
    description varchar(200)
);

insert into app_category (id,name,description) values ('web', '浏览器', 'web site、 h5');
insert into app_category (id,name,description) values ('ios', 'iOS平台', 'iOS平台');
insert into app_category (id,name,description) values ('android', 'Android平台', 'Android平台');
insert into app_category (id,name,description) values ('wechat', '微信平台', '微信平台');