delete from configurations where value = 'localhost' and key = 'buttonUrl';
alter table configurations alter column key set not null;
ALTER TABLE configurations
    ADD CONSTRAINT UNIQUE_key UNIQUE(key);
