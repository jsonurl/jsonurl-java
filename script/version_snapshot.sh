#!/bin/sh

mvn -B versions:set -DnewVersion=`sed -n 's/^parent.version=//p' snapshot.properties` -DartifactId=parent

mvn -B versions:set -DnewVersion=`sed -n 's/^module.version=//p' snapshot.properties` -DartifactId='*' -DprocessParent=false

