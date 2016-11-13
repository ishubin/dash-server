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


import net.mindengine.dashserver.controllers.DashboardApiController;
import net.mindengine.dashserver.controllers.DashboardController;

import static spark.Spark.staticFileLocation;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        DashboardStorage dashboardStorage = new DashboardStorageImpl("storage");

        staticFileLocation("/public");
        new DashboardApiController(dashboardStorage);
        new DashboardController();
    }

}
