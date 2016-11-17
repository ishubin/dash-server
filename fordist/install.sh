#!/bin/bash
set -e

if [ "$(id -u)" != "0" ]; then
    echo "You should run this script as root: sudo $0" 
    exit 1
fi

DEST=/opt

while getopts "d:" OPT; do
    case $OPT in
        d)
            DEST=$OPTARG
            ;;
    esac
done


mkdir -p  $DEST/dash-server
cp dash-server.jar $DEST/dash-server/.
cp dash-server $DEST/dash-server



ln -sf $DEST/dash-server/dash-server /usr/local/bin/dash-server

echo "Dash Server is successfully installed"

