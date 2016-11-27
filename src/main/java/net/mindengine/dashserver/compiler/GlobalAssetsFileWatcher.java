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
package net.mindengine.dashserver.compiler;

import net.mindengine.dashserver.assets.Asset;
import net.mindengine.dashserver.assets.ScriptAsset;
import net.mindengine.dashserver.assets.StyleAsset;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class GlobalAssetsFileWatcher extends Thread implements AssetProvider {
    private final String assetsFolderPath;
    private final File compiledAssetsFolder;
    private final String assetPrefix;
    private Set<Asset> assets = Collections.synchronizedSet(new HashSet<>());

    public GlobalAssetsFileWatcher(String assets, File compiledAssetsFolder, String assetPrefix) {
        this.assetsFolderPath = assets;
        this.compiledAssetsFolder = compiledAssetsFolder;
        this.assetPrefix = assetPrefix;
    }



    @Override
    public void run() {
        copyAllAssets();

        Path path = Paths.get(this.assetsFolderPath);
        FileSystem fileSystem = path.getFileSystem();
        try (WatchService service = fileSystem.newWatchService()) {
            path.register(service, ENTRY_MODIFY, ENTRY_CREATE);

            while (true) {
                WatchKey watchKey = service.take();

                for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                    Path watchEventPath = (Path) watchEvent.context();
                    String fileName = watchEventPath.toString();
                    copyFileAsset(fileName);
                }

                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyAllAssets() {
        File assetsFolder = new File(assetsFolderPath);
        File[] files = assetsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    if (file.isFile()) {
                        copyFileAsset(file.getName());
                    } else if (file.isDirectory()) {
                        copyDirectoryAsset(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyDirectoryAsset(File dir) throws IOException {
        String destFolder = compiledAssetsFolder.getAbsolutePath() + File.separator + dir.getName();
        FileUtils.copyDirectory(dir, new File(destFolder));
        Files.find(Paths.get(destFolder), 999, (p, bfa) -> bfa.isRegularFile()).forEach(file -> {

            String relativePath = new File(compiledAssetsFolder.getAbsolutePath()).toURI().relativize(file.toUri()).getPath();
            try {
                addItemToAssets(relativePath.replace("\\", "/"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyFileAsset(String fileName) throws IOException {
        copyFileToAssets(fileName);
        addItemToAssets(fileName);
    }

    private void addItemToAssets(String fileName) throws IOException {
        if (fileName.endsWith(".js")) {
            addScriptToAssets(fileName);
        } else if (fileName.endsWith(".css")) {
            addStyleToAssets(fileName);
        }
    }

    private void addStyleToAssets(String fileName) throws IOException {
        assets.add(new StyleAsset("/" + assetPrefix + "/" + fileName));
    }

    private void addScriptToAssets(String fileName) throws IOException {
        assets.add(new ScriptAsset("/" + assetPrefix + "/" + fileName));
    }

    private void copyFileToAssets(String fileName) throws IOException {
        FileUtils.copyFile(new File(assetsFolderPath + File.separator + fileName), new File(compiledAssetsFolder.getAbsolutePath() + File.separator + fileName));
    }

    @Override
    public Set<Asset> getAssets() {
        return assets;
    }
}
