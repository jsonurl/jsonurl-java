#!/bin/sh

PARENT_VERSION=`sed -n 's/^parent.version=//p' snapshot.properties`
MODULE_VERSION=`sed -n 's/^module.version=//p' snapshot.properties`
PARENT_VERSION_DEFAULT="1-SNAPSHOT"
MODULE_VERSION_DEFAULT="1.0.0-SNAPSHOT"
GROUP_ID="org.jsonurl"
M2_REPO="$HOME/.m2/repository/org/jsonurl"

test -d "$M2_REPO" && rm -fr "$M2_REPO" && echo "removed files from m2"

#
# install the parent so that 1-SNAPSHOT is still available for the
# children after I change the parent's version
#
(cd module && mvn -B install || exit 1) || exit 1

#
# set the project version of the parent
#   the ``cd'' is necessary to bypass a NPE in the maven versions plugin
#
(cd module && mvn -B versions:set \
	-DnewVersion="$PARENT_VERSION" \
	-DgroupId="$GROUP_ID" \
	-DartifactId='parent' \
	-DoldVersion="$PARENT_VERSION_DEFAULT"  \
	-DprocessProject=true \
	-DprocessParent=false \
|| exit 1) || exit 1

#
# install the parent again so the new version is available for the call to
# ``update-parent'' below
#
(cd module && mvn -B install || exit 1) || exit 1

# set the parent version for all modules
mvn -B versions:update-parent \
	-DparentVersion="$PARENT_VERSION" \
	-DallowSnapshots=true || exit 1

# set the project version for all modules
mvn -B versions:set \
	-DnewVersion="$MODULE_VERSION" \
	-DgroupId="$GROUP_ID" \
	-DartifactId='*' \
	-DoldVersion="$MODULE_VERSION_DEFAULT" \
	-DprocessAllModules=true \
	-DprocessProject=true \
	-DprocessParent=false || exit 1

test -d "$M2_REPO" && rm -fr "$M2_REPO" && echo "removed files from m2"

