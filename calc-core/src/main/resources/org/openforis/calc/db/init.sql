CREATE USER "calcdev" WITH PASSWORD 'calcdev';
CREATE USER "calcuser" WITH PASSWORD 'calcuser';

CREATE DATABASE calc ENCODING 'UTF8' OWNER calcdev;

\connect calc;

CREATE SCHEMA calc;
ALTER SCHEMA calc OWNER TO calcdev;