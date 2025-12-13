
create table exif_cache (
    image_id bigint not null primary key,
    -- Basic EXIF (from Exif class)
    serial_no int,
    shot_id bigint,
    shooting_at datetime not null,
    latitude double,
    longitude double,
    camera varchar(255) not null,
    -- Detail EXIF (from ExifDetail class)
    lens varchar(255),
    focal_length int comment 'Focal length in 35mm format',
    aperture decimal(5, 2) comment 'F-number',
    exposure_numerator int comment 'Exposure time numerator',
    exposure_denominator int comment 'Exposure time denominator',
    iso int,
    -- Metadata
    created_at datetime not null default current_timestamp,
    foreign key (image_id) references image(id) on delete cascade on update cascade
) engine=InnoDB default charset=utf8mb4;
