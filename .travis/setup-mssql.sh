#!/usr/bin/env bash

export PATH="$PATH:/opt/mssql-tools/bin"

sqlcmd -S localhost -U sa -P Password12! -Q "CREATE DATABASE staging" -d "master"
sqlcmd -S localhost -U sa -P Password12! -d staging -i ./.appveyor/create-brmo-persistence-sqlserver.sql