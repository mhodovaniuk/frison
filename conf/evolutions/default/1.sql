# --- !Ups
CREATE TABLE "Users" (
	id SERIAL PRIMARY KEY,
	email varchar(50) NOT NULL UNIQUE,
	password varchar(20) NOT NULL,
	activation_token varchar(50) NOT NULL,
	active boolean default(false) 
) 

CREATE TABLE "Files" (
	id SERIAL PRIMARY KEY,
	user_id bigint REFERENCES "Users"(id),
	name varchar(30) NOT NULL,
	size bigint,
	public boolean default(false)	
) 

# --- !Downs
drop table "Users"

drop table "Files"
