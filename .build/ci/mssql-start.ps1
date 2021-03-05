docker version
docker pull 'docker.b3p.nl/cautious-chainsaw/mssql-server-windows-developer:winsrv1809-sql2019'

printf "\n\nStarting MS SQL Server $1 container, this could take a while..."
# start the dockerized ms sql instance (the container will be destroyed/removed on stopping)
# this container can be stopped using: docker stop jdbcutil
docker run --rm --name jdbcutil -e ACCEPT_EULA=Y -e sa_password='Password12!' -p '1433:1433' -d 'docker.b3p.nl/cautious-chainsaw/mssql-server-windows-developer:winsrv1809-sql2019'

printf "\nWaiting for MS SQL Server $1 database to start up.... "
$WAIT = 0;
do
{
    printf " $WAIT"
    if (docker logs jdbcutil | Select-String -Quiet "Started SQL Server")
    {
        printf "\nSQL Server is now ready for client connections\n\n"
        break
    }
    sleep 10
    $WAIT = $WAIT + 10
} until ($WAIT -gt 120)

# print logs
docker logs jdbcutil