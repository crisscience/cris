@echo off
echo           New Installation of Database

mongo localhost:27017/cris create_user_and_db.js

echo Create user and database...
set PGPASSWORD=
psql -d postgres -U postgres -W -f  create_user_and_db.sql
