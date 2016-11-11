
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




Widget file system
/widgets/test-hub/test-hub.hbs
---------------------------------


/widgets/test-hub/test-hub.css
---------------------------------



/widgets/test-hub/test-hub.js
---------------------------------
Widgets.register("test-hub", new Widget({
    size: [1, 1],
    render: function (element, data) {
        $(element)
    }
}));
