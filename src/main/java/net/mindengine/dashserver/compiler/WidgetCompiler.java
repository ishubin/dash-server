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

import com.github.jknack.handlebars.io.FileTemplateLoader;
import net.mindengine.dashserver.assets.Asset;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import net.mindengine.dashserver.assets.ScriptAsset;
import net.mindengine.dashserver.assets.StyleAsset;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class WidgetCompiler implements AssetProvider {
    private final Set<Asset> assets = Collections.synchronizedSet(new HashSet<>());
    private final File compiledWidgetsFolder;
    private final String assetPrefix;
    private final File widgetsFolder;
    private final Handlebars handlebars;

    public WidgetCompiler(String widgetRootFolderPath, File compiledWidgetsFolder, String assetPrefix) {
        this.compiledWidgetsFolder = compiledWidgetsFolder;
        this.assetPrefix = assetPrefix;
        this.widgetsFolder = new File(widgetRootFolderPath);

        FileTemplateLoader templateLoader = new FileTemplateLoader(widgetsFolder, null);
        this.handlebars = new Handlebars(templateLoader);
    }

    public void compileAllWidgets() throws Exception {
        File[] widgetFolders = widgetsFolder.listFiles();
        if (widgetFolders != null) {
            for (File widgetFolder : widgetFolders) {
                if (widgetFolder.isDirectory()) {
                    assets.addAll(compileUserWidgetData(widgetFolder.getName()));
                }
            }
        }
    }

    public List<Asset> compileUserWidgetData(String widgetName) throws Exception {
        List<Asset> assets = new LinkedList<>();
        File widgetFolder = new File(widgetsFolder.getAbsolutePath() + File.separator + widgetName);
        File[] widgetItems = widgetFolder.listFiles();
        if (widgetItems != null) {
            for (File widgetItem : widgetItems) {
                if (widgetItem.getName().endsWith(".hbs")) {
                    assets.add(compileWidgetTemplate(widgetName, widgetFolder.getName() + File.separator + widgetItem.getName()));
                } else if (widgetItem.getName().endsWith(".scss")) {
                    assets.add(compileWidgetSass(widgetName, widgetItem));
                } else if (widgetItem.getName().endsWith(".js")) {
                    assets.add(copyWidgetScript(widgetName, widgetItem));
                } else if (widgetItem.getName().endsWith(".css")) {
                    assets.add(copyWidgetCss(widgetName, widgetItem));
                }
            }
        }

        System.out.println("Compiled data for widget: " + widgetName);
        return assets;
    }

    private StyleAsset copyWidgetCss(String widgetName, File widgetItem) throws IOException {
        String destName = copyWidgetFile(widgetName, widgetItem);
        return new StyleAsset(assetPrefix + destName);
    }

    private ScriptAsset copyWidgetScript(String widgetName, File widgetItem) throws IOException {
        String destName = copyWidgetFile(widgetName, widgetItem);
        return new ScriptAsset(assetPrefix + destName);
    }

    private String copyWidgetFile(String widgetName, File widgetItem) throws IOException {
        String destName = widgetName + "." + widgetItem.getName();
        File dest = new File(compiledWidgetsFolder.getAbsolutePath() + File.separator + destName);
        dest.createNewFile();
        FileUtils.copyFile(widgetItem, dest);
        return destName;
    }

    private StyleAsset compileWidgetSass(String widgetName, File widgetItem) throws Exception {
        SCSSErrorHandler errorHandler = new SCSSErrorHandler();

        File wrappedScss = wrapScssForWidget(widgetName, widgetItem);
        ScssStylesheet e = ScssStylesheet.get(wrappedScss.getAbsolutePath(), null, new SCSSDocumentHandlerImpl(), errorHandler);
        e.compile();
        String targetFileName = widgetName + "." + widgetItem.getName() + ".style.css";
        File targetFile = new File(compiledWidgetsFolder.getAbsolutePath() + File.separator + targetFileName);
        Writer writer = createWriter(targetFile);
        e.write(writer);
        writer.close();

        return new StyleAsset(assetPrefix + targetFileName);
    }

    private File wrapScssForWidget(String widgetName, File source) throws IOException {
        String content = FileUtils.readFileToString(source);
        File destFile = File.createTempFile(widgetName, ".scss");
        FileUtils.writeStringToFile(destFile, ".widget-type-" + widgetName +" {\n" + content + "\n}");
        return destFile;
    }

    private static Writer createWriter(File targetFile) throws IOException {
        try {
            targetFile.createNewFile();
        } catch (IOException ex) {
            throw new IOException("Cannot create file: " + targetFile.getAbsolutePath(), ex);
        }
        return new FileWriter(targetFile);
    }

    private ScriptAsset compileWidgetTemplate(String widgetName, String path) throws IOException {
        Template template = handlebars.compile(path);
        String js = template.toJavaScript();
        String builder = "(function() {var template = Handlebars.template(" +
            js +
            ");\n" +
            "  var templates = Handlebars.templates = Handlebars.templates || {};\n" +
            "  templates['widget-" + widgetName + "'] = template;\n" +
            "  var partials = Handlebars.partials = Handlebars.partials || {};\n" +
            "  partials['widget-" + widgetName + "'] = template;\n" +
            "})();";
        String destName = widgetName + ".template.js";
        FileUtils.writeStringToFile(new File(compiledWidgetsFolder.getAbsolutePath() + File.separator + destName), builder);
        return new ScriptAsset(assetPrefix + destName);
    }

    @Override
    public Set<Asset> getAssets() {
        return assets;
    }
}
