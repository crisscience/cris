db.dropDatabase();
db.createUser({user: "test", pwd: "c1234c", roles: [{role: "readWrite", db: "test"}]});
