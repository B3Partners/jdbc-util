#!/usr/bin/env bash
set -e
export SQLPATH=./.build/ci
printf "\nSetup database...\n"


docker exec -i jdbcutil sqlplus -l sys/oracle@//localhost:1521/FREE as sysdba < ./.build/ci/oracle-system-setup.sql
docker exec -i jdbcutil sqlplus -s /nolog<<EOF
conn sys/oracle as sysdba
startup
exit
EOF

# note port number is the port inside the container!
docker exec -i jdbcutil sqlplus -l system/oracle@//localhost:1521/FREE < ./.build/ci/oracle-create-user.sql
docker exec -i jdbcutil sqlplus -l jdbcutil/jdbcutil@//localhost:1521/FREE < ./.build/ci/oracle-create-schema.sql
