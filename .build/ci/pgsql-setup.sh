#!/usr/bin/env bash

psql -U postgres -h localhost -a -c "CREATE ROLE staging LOGIN PASSWORD 'staging' SUPERUSER CREATEDB;"
psql -U postgres -h localhost -c 'CREATE DATABASE staging;'
psql -U postgres -h localhost -d staging -c 'CREATE EXTENSION postgis;'
psql -U postgres -h localhost -d staging -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -h localhost -d staging -c 'SELECT PostGIS_full_version();'
psql -U postgres -h localhost -d staging -f .build/ci/pgsql-create-schema.sql