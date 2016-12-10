#!/bin/bash
set +e
. base.sh

echo Creating dashboard
post_json "dashboards" '{
    "name": "demo",
    "settings": {
        "default": {
            "layout": "fixed",
            "columns": 5,
            "rows": 5
        },
        "large": {
            "layout": "flex",
            "cellWidth": 140,
            "cellHeight": 150
        }
    }
}'
