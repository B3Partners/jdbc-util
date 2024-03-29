#!/usr/bin/env bash
docker version

# this docker image has the following users/credentials (user/password = system/oracle)
docker pull ghcr.io/gvenzl/$1
docker run --rm -p 15210:1521 -e ORACLE_PASSWORD=oracle --name jdbcutil -h jdbcutil -d ghcr.io/gvenzl/$1

# start the dockerized oracle-xe instance
# this container can be stopped using:
#
#    docker stop jdbcutil
#
printf "\n\nStarting Oracle $1 ontainer, this could take a few minutes..."
printf "\nWaiting for Oracle $1 database to start up.... "
_WAIT=0;
while :
do
    printf " $_WAIT"
    if $(docker logs jdbcutil | grep -q 'DATABASE IS READY TO USE!'); then
        printf "\nOracle $1 Database started\n\n"
        break
    fi
    sleep 10
    _WAIT=$(($_WAIT+10))
done

# docker ps -a
# print logs
docker logs jdbcutil
