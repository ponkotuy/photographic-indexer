
alter table tag add constraint chk_tag_name_not_empty check (name <> '');
