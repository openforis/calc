CREATE USER "%{calc.jdbc.username}" WITH PASSWORD '%{calc.jdbc.password}';

CREATE DATABASE "%{calc.jdbc.db}" ENCODING 'UTF8' OWNER "%{calc.jdbc.username}";

\connect "%{calc.jdbc.db}";

CREATE SCHEMA "calc" AUTHORIZATION "%{calc.jdbc.username}";