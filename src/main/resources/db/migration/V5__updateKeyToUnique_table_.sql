alter table configurations alter column key set not null;

create unique index configurations_key_uindex
    on configurations (key);

