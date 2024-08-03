#!/usr/bin/env bash
printf "\nSetup database...\n"
docker exec -i jdbcutil /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Password12!" -No -d "master" -Q 'CREATE DATABASE staging'
docker cp ./.build/ci/mssql-create-schema.sql jdbcutil:/home/
docker exec -i jdbcutil /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Password12!" -No -d "staging" -i /home/mssql-create-schema.sql
