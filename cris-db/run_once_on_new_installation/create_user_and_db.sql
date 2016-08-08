DROP DATABASE IF EXISTS cris;
DROP ROLE IF EXISTS cris;

CREATE ROLE cris LOGIN NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE PASSWORD 'c1234c';
CREATE DATABASE cris
  WITH OWNER = cris
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
