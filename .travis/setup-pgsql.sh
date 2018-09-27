#!/usr/bin/env bash

psql --version
psql -U postgres -d postgres -c 'SELECT Version();'
psql -U postgres -a -c "CREATE ROLE staging LOGIN PASSWORD 'staging' SUPERUSER CREATEDB;"
psql -U postgres -c 'CREATE DATABASE staging;'
psql -U postgres -d staging -c 'CREATE EXTENSION postgis;'
psql -U postgres -d staging -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -d staging -c 'SELECT PostGIS_full_version();'
psql -U postgres -d staging -f .travis/create-brmo-persistence-postgresql.sql