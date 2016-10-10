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

package org.eightlog.thumty.feature;

import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link org.eightlog.thumty.feature.FeatureRegion}.
 *
 * NOTE: This class has been automatically generated from the {@link org.eightlog.thumty.feature.FeatureRegion} original class using Vert.x codegen.
 */
public class FeatureRegionConverter {

  public static void fromJson(JsonObject json, FeatureRegion obj) {
    if (json.getValue("height") instanceof Number) {
      obj.setHeight(((Number)json.getValue("height")).intValue());
    }
    if (json.getValue("weight") instanceof Number) {
      obj.setWeight(((Number)json.getValue("weight")).doubleValue());
    }
    if (json.getValue("width") instanceof Number) {
      obj.setWidth(((Number)json.getValue("width")).intValue());
    }
    if (json.getValue("x") instanceof Number) {
      obj.setX(((Number)json.getValue("x")).intValue());
    }
    if (json.getValue("y") instanceof Number) {
      obj.setY(((Number)json.getValue("y")).intValue());
    }
  }

  public static void toJson(FeatureRegion obj, JsonObject json) {
    json.put("height", obj.getHeight());
    json.put("weight", obj.getWeight());
    json.put("width", obj.getWidth());
    json.put("x", obj.getX());
    json.put("y", obj.getY());
  }
}