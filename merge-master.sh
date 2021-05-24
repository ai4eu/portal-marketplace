#!/bin/bash -x
zip saved-poms.zip pom.xml acumos-portal-be/pom.xml acumos-portal-fe/pom.xml
git pull
git merge -X theirs origin/master
unzip -o saved-poms.zip

