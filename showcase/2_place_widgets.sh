#!/bin/bash
set +e
. base.sh

echo Posting Widgets
post_json "dashboards/demo/widgets" '
{
    "graphs": {
        "width": 2,
        "height": 2,
        "sortOrder": "0-graphs",
        "widgetType": "graph-bars",
        "data": {
            "title": "Test Statistics"
        }
    },
    "selenium-tests": {
        "width": 1,
        "height": 1,
        "sortOrder": "1-selenium-tests",
        "widgetType": "test",
        "data": {
            "link": "http://example.com/selenium-tests",
            "title": "Selenium Tests",
            "icon": "selenium",
            "failedTests": 2,
            "isRunning": true,
            "status": "failed"
        }
    },
    "api-tests": {
        "width": 1,
        "height": 1,
        "sortOrder": "1-api-tests",
        "widgetType": "test",
        "data": {
            "link": "http://example.com/api-tests",
            "title": "API Tests",
            "icon": "api",
            "failedTests": 0,
            "isRunning": false,
            "status": "passed"
        }
    },
    "db-tests": {
        "width": 1,
        "height": 1,
        "sortOrder": "1-db-tests",
        "widgetType": "test",
        "data": {
            "link": "http://example.com/db-tests",
            "title": "DB Tests",
            "icon": "db",
            "failedTests": 1,
            "isRunning": false,
            "status": "failed"
        }
    }
}
'

