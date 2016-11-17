#!/bin/bash

set -e

mkdir -p dist

if [ -d dist ]; then
    rm -rf dist/*
fi


mvn clean assembly:assembly
cp target/dash-server-jar-with-dependencies.jar dist/dash-server.jar
cp fordist/dash-server dist/.
cp fordist/install.sh dist/.

