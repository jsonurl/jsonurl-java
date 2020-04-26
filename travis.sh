#!/bin/sh

MAVEN_DO_PROFILES=
MAVEN_DO_PHASES=

$JAVA_HOME/bin/java -version 2>&1 | egrep -qsi 'version[^0-9]+1\.8' && MAVEN_DO_PROFILES='-P jacoco' && MAVEN_DO_PHASES='sonar:sonar'


mvn                                         \
    -Dsonar.projectKey=jsonurl_jsonurl-java \
    $MAVEN_DO_PROFILES                      \
    clean compile test install              \
    $MAVEN_DO_PHASES

