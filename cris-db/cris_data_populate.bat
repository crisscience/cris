@echo off
echo           Populate the Database

set PGPASSWORD=c1234c
psql -1 -d cris -U cris -f cris_data.sql

set PGPASSWORD=
