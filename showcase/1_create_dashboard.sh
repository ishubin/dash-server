#!/bin/bash
set +e
. base.sh

echo Creating dashboard
post_json "dashboards" '{"name": "demo"}'


