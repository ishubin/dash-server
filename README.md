
POST /api/dashboards
{
    "name": "placing"
}


POST /api/dashboards/:dashboardName/widgets
{
    "selenium-tests": {
        "type": "test-hub",
        "visible": true,
        "data": {
            ...
        }
    }
]


PUT /api/dashboards/:dashboardName/widgets/:widgetId
{
    "widgetId": "selenium-tests",
    "type": "test-hub",
    "visible": true,
    "data": {
        ...
    }
}

PUT /api/dashboards/:dashboardName/widgets/:widgetId/data
{
    ...
}


GET PUT /api/dashboards/:dashboardName/widgets


DELETE /api/dashboards/:dashboardName/widgets/:widgetId



