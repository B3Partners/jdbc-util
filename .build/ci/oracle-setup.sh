#!/usr/bin/env bash
printf "\nSetup database...\n"

# note port number is the port inside the container!
docker exec -i jdbcutil sqlplus -l system/oracle@//localhost:1521/FREE < ./.build/ci/oracle-create-user.sql
docker exec -i jdbcutil sqlplus -l jdbcutil/jdbcutil@//localhost:1521/FREE < ./.build/ci/oracle-create-schema.sql
