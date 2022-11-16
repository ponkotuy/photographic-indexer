
create table tag (
    id bigint not null auto_increment primary key,
    name varchar(63) not null,
    unique key (name)
) engine=InnoDB default charset=utf8mb4;

create table image_tag (
    image_id bigint not null,
    tag_id bigint not null,
    foreign key (image_id) references image(id) on delete cascade on update cascade,
    foreign key (tag_id) references tag(id) on delete cascade on update cascade,
    unique key (image_id, tag_id)
) engine=InnoDB default charset=utf8mb4;
