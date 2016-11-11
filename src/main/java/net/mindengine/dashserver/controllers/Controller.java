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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.*;

import java.io.IOException;

import static net.mindengine.dashserver.JsonTransformer.toJson;
import static spark.Spark.post;
import static spark.Spark.get;

public class Controller {

    protected ObjectMapper objectMapper = new ObjectMapper();

    public <T> T fromJson(Request req, Class<T> clazz) throws IOException {
        return objectMapper.readValue(req.body(), clazz);
    }

    public <T> T fromJson(Request req, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(req.body(), typeReference);
    }

    public static void postJson(String path, Route route) {
        post(path, route, toJson());
    }

    public static void postJson(String path, String acceptType, Route route) {
        post(path, acceptType, route, toJson());
    }

    public static void getJson(String path, Route route) {
        get(path, route, toJson());
    }
}
