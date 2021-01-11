#!/bin/bash
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./.build/ci/oracle-create-schema.sql
