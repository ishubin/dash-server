/*******************************************************************************
 * Copyright 2016 Ivan Shubin https://github.com/ishubin/dash-server
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mindengine.dashserver.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import net.mindengine.dashserver.DashboardStorage;
import net.mindengine.dashserver.model.DashboardRequest;
import net.mindengine.dashserver.model.WidgetRequest;

import java.util.HashMap;
import java.util.Map;

public class DashboardApiController extends Controller {
    private TypeReference<HashMap<String,WidgetRequest>> WIDGET_REQUESTS_TYPE = new TypeReference<HashMap<String, WidgetRequest>>() {};

    private final DashboardStorage dashboardStorage;

    public DashboardApiController(DashboardStorage dashboardStorage) {
        this.dashboardStorage = dashboardStorage;
        init();
    }

    private void init() {
        postJson("/api/dashboards", (req, res) -> {
            DashboardRequest dashboardRequest = fromJson(req, DashboardRequest.class);
            dashboardStorage.createDashboard(dashboardRequest);
            return dashboardRequest;
        });

        getJson("/api/dashboards/:dashboardName", (req, res) ->  {
            String dashboardName = req.params("dashboardName");
            return dashboardStorage.findDashboard(dashboardName);
        });

        getJson("/api/dashboards/:dashboardName/widgets", (req, res) ->  {
            String dashboardName = req.params("dashboardName");
            return dashboardStorage.findDashboard(dashboardName).getWidgets();
        });

        postJson("/api/dashboards/:dashboardName/widgets", (req, res) -> {
            String dashboardName = req.params("dashboardName");
            Map<String, WidgetRequest> widgetRequests = fromJson(req, WIDGET_REQUESTS_TYPE);
            dashboardStorage.updateWidgets(dashboardName, widgetRequests);
            return widgetRequests;
        });

        postJson("/api/dashboards/:dashboardName/widgets/:widgetName", (req, res) -> {
            String dashboardName = req.params("dashboardName");
            String widgetName = req.params("widgetName") ;
            WidgetRequest widgetRequest = fromJson(req, WidgetRequest.class);
            dashboardStorage.updateWidget(dashboardName, widgetName, widgetRequest);
            return widgetRequest;
        });

        deleteJson("/api/dashboards/:dashboardName/widgets/:widgetName", (req, res) -> {
            String dashboardName = req.params("dashboardName");
            String widgetName = req.params("widgetName") ;
            dashboardStorage.removeWidget(dashboardName, widgetName);
            return "removed widget " + widgetName;
        });
    }
}
