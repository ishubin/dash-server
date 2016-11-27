#!/bin/bash

set -e

mkdir -p dist

if [ -d dist ]; then
    rm -rf dist/*
fi

version=$( cat pom.xml | grep "<version>" | head -n 1 | awk -F"[<>]" '/version/{print $3}' | sed "s/-SNAPSHOT//g" )
bin=dash-server-${version}
mkdir -p dist/$bin

echo New dist is $version
echo Assemblying new dist

mvn clean assembly:assembly

cp target/dash-server-jar-with-dependencies.jar dist/$bin/dash-server.jar
cp fordist/dash-server dist/$bin/.
cp fordist/install.sh dist/$bin/.
cp LICENSE-2.0.txt dist/$bin/.
cp README.md dist/$bin/.


cd dist
zip -r -9 $bin.zip $bin

cd ..

