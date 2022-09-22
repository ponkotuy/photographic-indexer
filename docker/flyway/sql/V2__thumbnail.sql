
create table thumbnail (
    image_id bigint not null primary key,
    file mediumblob not null,
    created_at datetime not null,
    foreign key (image_id) references image(id) on delete cascade on update cascade
) engine=InnoDB default charset=utf8mb4;
