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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mindengine.dashserver.model.Dashboard;
import net.mindengine.dashserver.model.WidgetRequest;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashboardStorageImpl implements DashboardStorage {
    private final File dashboardStorageDir;
    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Dashboard> dashboards = new ConcurrentHashMap<>();

    public DashboardStorageImpl(String storagePath) {
        dashboardStorageDir = new File(storagePath + File.separator + "dashboards");
        makeSureDirectoryExists(dashboardStorageDir);
        loadFromStorage();
    }

    private void makeSureDirectoryExists(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Couldn't create directory: " + dir.getAbsolutePath());
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("Not a directory: " + dir.getAbsolutePath());
        }
    }

    private void loadFromStorage() {
        File[] files = dashboardStorageDir.listFiles();
        for (File dashboardFile : files) {
            loadDashboard(dashboardFile);
        }
    }

    private void loadDashboard(File dashboardFile) {
        try {
            Dashboard dashboard = new Dashboard(objectMapper.readValue(dashboardFile, Dashboard.class));
            dashboard.getWidgets().putAll(dashboard.getWidgets());
            dashboards.put(dashboard.getName(), dashboard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(Dashboard dashboard) {
        try {
            synchronized (dashboard.getName().intern()) {
                File dashboardFile = new File(dashboardStorageDir.getAbsolutePath() + File.separator + dashboard.getName() + ".json");
                dashboardFile.createNewFile();
                objectMapper.writeValue(dashboardFile, dashboard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createDashboard(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name should be defined");
        }

        synchronized (name.intern()) {
            if (dashboards.containsKey(name)) {
                throw new IllegalArgumentException("Dashboard already exists with name: " + name);
            }
            Dashboard dashboard = new Dashboard(name);
            dashboards.put(name, dashboard);
            save(dashboard);
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

        save(dashboard);
    }

    @Override
    public void updateWidget(String dashboardName, String widgetName, WidgetRequest widgetRequest) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            dashboard.putWidget(widgetName, widgetRequest.asWidget());
        }

        save(dashboard);
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

    @Override
    public void removeWidget(String dashboardName, String widgetName) {
        Dashboard dashboard = dashboards.get(dashboardName);
        if (dashboard != null) {
            dashboard.getWidgets().remove(widgetName);
        } else {
            throw new RuntimeException("Dashboard does not exist: " + dashboardName);
        }

        save(dashboard);
    }
}
