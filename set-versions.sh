#!/bin/bash -x
mvn versions:set -DnewVersion=$1 -DprocessAllModules -DgenerateBackupPoms=false
