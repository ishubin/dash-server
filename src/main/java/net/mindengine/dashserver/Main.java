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


import com.google.common.io.Files;
import net.mindengine.dashserver.compiler.GlobalAssetsFileWatcher;
import net.mindengine.dashserver.compiler.WidgetCompiler;
import net.mindengine.dashserver.controllers.DashboardApiController;
import net.mindengine.dashserver.controllers.DashboardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.port;
import static spark.Spark.staticFileLocation;


public class Main {
    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        DashServerProperties properties = new DashServerProperties();

        port(properties.getPort());

        File tempFolder = Files.createTempDir();
        LOG.info("Widgets compilation folder: " + tempFolder.getAbsolutePath());
        externalStaticFileLocation(tempFolder.getAbsolutePath());
        staticFileLocation("/public");

        File compiledWidgetsFolder = createWidgetsCompileFolder(tempFolder);
        File globalAssetsFolder = createGlobalAssetsFolder(tempFolder);

        WidgetCompiler widgetCompiler = new WidgetCompiler("widgets", compiledWidgetsFolder, "/_widgets_/");
        widgetCompiler.compileAllWidgets();

        new WidgetDataFileWatcher("widgets", widgetCompiler).start();
        GlobalAssetsFileWatcher globalAssetWatcher = new GlobalAssetsFileWatcher("assets", globalAssetsFolder, "_global_assets_");
        globalAssetWatcher.start();

        DashboardStorage dashboardStorage = new DashboardStorageImpl("storage");

        new DashboardApiController(dashboardStorage);
        new DashboardController(() ->
            Stream.concat(widgetCompiler.getAssets().stream(), globalAssetWatcher.getAssets().stream()).collect(Collectors.toSet())
        );
    }

    private static File createGlobalAssetsFolder(File tempFolder) throws IOException {
        File globalAssetsFolder = new File(tempFolder.getAbsolutePath() + File.separator + "_global_assets_");
        if (!globalAssetsFolder.mkdirs()) {
            throw new IOException("Couldn't create directory: " + globalAssetsFolder.getAbsolutePath());
        }
        return globalAssetsFolder;
    }

    private static File createWidgetsCompileFolder(File tempFolder) throws IOException {
        File compiledWidgetsFolder = new File(tempFolder.getAbsolutePath() + File.separator + "_widgets_");
        if (!compiledWidgetsFolder.mkdirs()) {
            throw new IOException("Couldn't create directory: " + compiledWidgetsFolder.getAbsolutePath());
        }
        return compiledWidgetsFolder;
    }
    private static String property(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }


}
