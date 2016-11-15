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

import com.github.jknack.handlebars.Handlebars;
import net.mindengine.dashserver.compiler.AssetProvider;


import static java.util.stream.Collectors.toList;

public class DashboardController extends Controller {

    private final AssetProvider assetProvider;

    public DashboardController(AssetProvider assetProvider) {
        this.assetProvider = assetProvider;
        init();
    }

    private void init() {
        getHsTpl("/dashboards/:dashboardName", "dashboard", (req, model) -> {
            model.put("dashboardName", req.params("dashboardName"));
            model.put("widgetAssets", assetProvider.getAssets().stream()
                .map(wa -> new Handlebars.SafeString(wa.getAsset()))
                .collect(toList())
            );
        });
    }

}
