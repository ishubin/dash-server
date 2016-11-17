#!/bin/bash
set +e
. base.sh

echo Creating dashboard
post_json "dashboards" '{
    "name": "demo",
    "settings": {
        "default": {
            "cellSize": {
                "width": 150,
                "height": 100
            }
        },
        "large": {
            "cellSize": {
                "width": 250,
                "height": 200
            }
        }
    }
}'


