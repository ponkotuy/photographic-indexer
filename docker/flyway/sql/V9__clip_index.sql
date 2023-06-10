create table image_clip_index (
    image_id bigint not null primary key,
    clip_index JSON not null,
    foreign key (image_id) references image(id) on delete cascade on update cascade
) engine=innoDB default charset=utf8mb4;
