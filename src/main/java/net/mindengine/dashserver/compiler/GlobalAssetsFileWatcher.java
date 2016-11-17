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
                    copyAsset(fileName);
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
        for (File file : assetsFolder.listFiles()) {
            if (file.isFile()) {
                try {
                    copyAsset(file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyAsset(String fileName) throws IOException {
        if (fileName.endsWith(".js")) {
            copyScript(fileName);
        } else if (fileName.endsWith(".css")) {
            copyStyle(fileName);
        }
    }

    private void copyStyle(String fileName) throws IOException {
        copyFileToAssets(fileName);
        assets.add(new StyleAsset("/" + assetPrefix + "/" + fileName));
    }

    private void copyScript(String fileName) throws IOException {
        copyFileToAssets(fileName);
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
