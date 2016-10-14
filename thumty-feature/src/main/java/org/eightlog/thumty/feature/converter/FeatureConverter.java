/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.eightlog.thumty.feature.converter;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.image.geometry.Feature;
import org.eightlog.thumty.image.geometry.FeatureType;

import java.awt.*;

public class FeatureConverter {

    public static Feature fromJson(JsonObject json) {
        int x = json.getInteger("x", 0);
        int y = json.getInteger("y", 0);
        int width = json.getInteger("width", 0);
        int height = json.getInteger("height", 0);
        double weight = json.getDouble("weight", 0D);
        FeatureType type = FeatureType.valueOf(json.getString("type", FeatureType.COMMON.name()));

        return new Feature(new Rectangle(x, y, width, height), type, weight);
    }

    public static JsonObject toJson(Feature feature) {
        JsonObject json = new JsonObject();

        json.put("x", feature.getShape().x);
        json.put("y", feature.getShape().y);
        json.put("width", feature.getShape().width);
        json.put("height", feature.getShape().height);
        json.put("weight", feature.getWeight());
        json.put("type", feature.getType().name());

        return json;
    }
}