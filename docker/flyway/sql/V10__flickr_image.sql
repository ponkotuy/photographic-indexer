
create table flickr_image (
    image_id bigint not null primary key,
    url varchar(767) not null
) engine=InnoDB default charset=utf8mb4;
