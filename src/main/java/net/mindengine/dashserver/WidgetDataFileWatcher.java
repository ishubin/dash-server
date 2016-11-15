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

import net.mindengine.dashserver.compiler.WidgetCompiler;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class WidgetDataFileWatcher extends Thread {

    private final String widgetsFolderPath;
    private final WidgetCompiler widgetCompiler;

    public WidgetDataFileWatcher(String widgetsFolderPath, WidgetCompiler widgetCompiler) {
        this.widgetsFolderPath = widgetsFolderPath;
        this.widgetCompiler = widgetCompiler;
    }


    @Override
    public void run() {
        Path path = Paths.get(widgetsFolderPath);
        FileSystem fileSystem = path.getFileSystem();
        try (WatchService service = fileSystem.newWatchService()) {
            path.register(service, ENTRY_MODIFY, ENTRY_CREATE);

            while (true) {
                WatchKey watchKey = service.take();

                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    Path watchEventPath = (Path) watchEvent.context();
                    String widgetName = watchEventPath.toString();
                    recompileWidgetData(widgetName);
                }

                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recompileWidgetData(String widgetName) throws Exception {
        widgetCompiler.compileUserWidgetData(widgetName);
    }
}

