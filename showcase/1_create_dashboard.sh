#!/bin/bash
set +e
. base.sh

echo Creating dashboard
post_json "dashboards" '{
    "name": "demo",
    "settings": {
        "cellSize": {
            "width": 150,
            "height": 100
        }
    }
}'


