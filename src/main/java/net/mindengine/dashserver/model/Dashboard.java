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
package net.mindengine.dashserver.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dashboard {
    private String name;

    private Map<String, Widget> widgets = new ConcurrentHashMap<>();

    public Dashboard() {
    }

    public Dashboard(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Widget> getWidgets() {
        return widgets;
    }

    public void setWidgets(Map<String, Widget> widgets) {
        this.widgets = widgets;
    }

    public void putWidget(String widgetId, Widget widget) {
        widgets.put(widgetId, widget);
    }
}
