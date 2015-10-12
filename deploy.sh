#!/bin/sh

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters"
    exit -1
fi

RELEASE_VERSION=$1
DEVELOPMENT_VERSION=$2

S3_UPDATES_BUCKET_NAME=updates-ovation-io
S3_DOWNLOADS_BUCKET_NAME=download.ovation.io

echo "Deploying version v${RELEASE_VERSION}"
echo " "

git tag | grep -w v${RELEASE_VERSION};
if [ $? == 0 ]; then echo "FAILED: release version already exists"; exit 1; fi


mvn versions:set -DnewVersion=${RELEASE_VERSION}
mvn versions:commit

if [[ $? != 0 ]]; then echo "FAILED" && exit 1; fi

#update the manifest versions for the netbeans modules
./updateVersion.py ${RELEASE_VERSION}

mvn -Psign --projects application --also-make clean install -DskipTests
if [[ $? != 0 ]]; then echo "FAILED" && exit 1; fi

mvn -Pdeployment,deploy-dmg,sign  --projects application install -DskipTests
if [[ $? != 0 ]]; then echo "FAILED" && exit 1; fi


git add .
git commit -m "prepare release ${RELEASE_VERSION}"

git tag v${RELEASE_VERSION}
git push --tags

# Update for development SNAPSHOT
echo "Updating API version to ${DEVELOPMENT_VERSION}"

mvn versions:set -DnewVersion=${DEVELOPMENT_VERSION}
mvn versions:commit

if [[ $? != 0 ]]; then echo "FAILED" && exit 1; fi

git add .
git commit -m "update to snapshot version ${DEVELOPMENT_VERSION}"
git push origin master
