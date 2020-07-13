#!/bin/sh

comment="$1"
url="$2"

git config --global user.email "$GITHUB_ACTOR"
git config --global user.name "ci action"

mkdir -p ~/.ssh
chmod 700 ~/.ssh
cat >~/.ssh/id_ed25519 << EOT
-----BEGIN OPENSSH PRIVATE KEY-----
$GH_ACTION_KEY
-----END OPENSSH PRIVATE KEY-----
EOT
chmod 600 ~/.ssh/id_ed25519

#    "-Dscmpublish.pubScmUrl=$url" \
#    '-Dscmpublish.dryRun=true' \
#    "-Dusername=git" \
#    "-Dpassword=$GITHUB_TOKEN" \
exec mvn "-Dscmpublish.checkinComment=$comment" \
    -B -Ppublish-javadoc-site javadoc:aggregate scm-publish:publish-scm
