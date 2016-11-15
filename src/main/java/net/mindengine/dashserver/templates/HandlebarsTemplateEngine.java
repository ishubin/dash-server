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
package net.mindengine.dashserver.templates;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HandlebarsTemplateEngine extends TemplateEngine {
    private Handlebars handlebars;
    private Map<String, Template> compiledTemplates = new HashMap<>();

    public HandlebarsTemplateEngine(String templatePath) {
        ClassPathTemplateLoader classPathTemplateLoader = new ClassPathTemplateLoader();
        classPathTemplateLoader.setPrefix(templatePath);
        classPathTemplateLoader.setSuffix(".hbs");
        this.handlebars = new Handlebars(classPathTemplateLoader);
        initHelpers();
    }

    private void initHelpers() {
        this.handlebars.registerHelper("ifGreaterThan", (var, options) -> {
            long a = ((Number)var).longValue();
            long b = ((Number)options.param(0)).longValue();
            if (a > b) {
                return options.fn(var);
            } else {
                return options.inverse(var);
            }
        });
    }

    public String render(ModelAndView modelAndView) {
        String viewName = modelAndView.getViewName();

        try {
            Template template = findTemplate(viewName);
            return template.apply(modelAndView.getModel());
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't render template: " + viewName, ex);
        }
    }

    private Template findTemplate(String viewName) throws IOException {
        Template template;
        synchronized (viewName.intern()) {
            if (compiledTemplates.containsKey(viewName)) {
                template = compiledTemplates.get(viewName);
            } else {
                template = this.handlebars.compile(viewName);
                compiledTemplates.put(viewName, template);
            }
        }
        return template;
    }

    public Handlebars getHandlebars() {
        return handlebars;
    }
}
