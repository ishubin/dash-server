#!/bin/bash

set -e
./makeDist.sh

bin=$(find dist -type d | grep dash-server)

cd $bin
pwd
sudo ./install.sh
