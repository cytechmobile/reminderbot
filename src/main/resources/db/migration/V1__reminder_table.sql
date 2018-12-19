CREATE TABLE reminder(
 what VARCHAR (50),
 whento TIMESTAMP,
 reminder_id serial PRIMARY KEY,
 space_id VARCHAR (15),
 thread_id VARCHAR (15),
 sender_displayname VARCHAR (50)
);