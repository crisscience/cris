echo           Populate the Database

export PGPASSWORD=c1234c
psql -1 -d cris -U cris -f cris_data.sql

export PGPASSWORD=
