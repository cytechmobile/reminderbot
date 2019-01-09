
ALTER TABLE reminder
ADD COLUMN reminder_timezone VARCHAR (35);

create table time_zone(
timezone varchar (50),
userid varchar (50)
);