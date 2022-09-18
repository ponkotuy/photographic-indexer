
create table geom (
    id bigint not null auto_increment primary key,
    address varchar(511) not null,
    latlon point srid 4326 not null,
    spatial index(latlon),
    fulltext (address) with parser ngram
) engine=InnoDB default charset=utf8mb4;

create table image (
    id bigint not null auto_increment primary key,
    camera_id int not null,
    shot_id int not null,
    shooting_at datetime not null,
    geo_id bigint,
    unique key (camera_id, shot_id),
    foreign key (geo_id) references geom(id) on delete cascade on update cascade
) engine=InnoDB default charset=utf8mb4;

create table image_file (
    id bigint not null auto_increment primary key,
    image_id bigint not null ,
    path varchar(767) not null,
    filesize bigint not null,
    unique key (path),
    foreign key (image_id) references image(id)
) engine=InnoDB default charset=utf8mb4;
