
alter table image add note varchar(767);

alter table image add fulltext index image_note_fulltext(note) with parser ngram;
