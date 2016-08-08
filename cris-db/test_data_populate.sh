echo "*******************************************************************"
echo "******** Fresh Install of MongoDB and PostgreSQL Test Data ********"
echo "*******************************************************************"

mongo localhost:27017/test test_user_and_db.js

mongo localhost:27017/test test_data.js

echo "Create test user and test database..."
psql -d postgres -U postgres -W -f  test_user_and_db.sql

export PGPASSWORD=c1234c
bin/migrate up --path=cris --env=testing && psql -1 -d test -U test -f test_data.sql
export PGPASSWORD=
