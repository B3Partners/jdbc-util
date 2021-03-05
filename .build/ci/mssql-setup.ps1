printf "\nSetup database...\n"
docker exec -i jdbcutil sqlcmd -S localhost -U SA -P 'Password12!' -Q 'SELECT @@version' -d 'master'
docker exec -i jdbcutil sqlcmd -S localhost -U SA -P 'Password12!' -Q 'CREATE DATABASE staging' -d 'master'
docker cp .\.build\ci\mssql-create-schema.sql jdbcutil:C:\
docker exec -i jdbcutil sqlcmd -S localhost -U SA -P 'Password12!' -d 'staging' -i 'C:\mssql-create-schema.sql'