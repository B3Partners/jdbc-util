#!/bin/bash
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.11:1521/ORCL < ./.jenkins/create-brmo-persistence-oracle.sql
