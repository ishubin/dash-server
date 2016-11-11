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
package net.mindengine.dashserver;

import net.mindengine.dashserver.model.Dashboard;
import net.mindengine.dashserver.model.WidgetRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardStorageImpl implements DashboardStorage {

    private Map<String, Dashboard> dashboards = new ConcurrentHashMap<>();


    @Override
    public void createDashboard(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name should be defined");
        }

        synchronized (name.intern()) {
            if (dashboards.containsKey(name)) {
                throw new IllegalArgumentException("Dashboard already exists with name: " + name);
            }
            dashboards.put(name, new Dashboard(name));
        }
    }

    @Override
    public void updateWidgets(String dashboardName, Map<String, WidgetRequest> widgetRequests) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            widgetRequests.entrySet().forEach(w ->
                dashboard.putWidget(w.getKey(), w.getValue().asWidget())
            );
        }
    }

    @Override
    public void updateWidget(String dashboardName, String widgetName, WidgetRequest widgetRequest) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            dashboard.putWidget(widgetName, widgetRequest.asWidget());
        }
    }

    @Override
    public Dashboard findDashboard(String dashboardName) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            return dashboard;
        } else {
            throw new RuntimeException("Dashboard does not exist: " + dashboardName);
        }
    }
}
