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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link org.eightlog.thumty.feature.Features}.
 *
 * NOTE: This class has been automatically generated from the {@link org.eightlog.thumty.feature.Features} original class using Vert.x codegen.
 */
public class FeaturesConverter {

  public static void fromJson(JsonObject json, Features obj) {
    if (json.getValue("height") instanceof Number) {
      obj.setHeight(((Number)json.getValue("height")).intValue());
    }
    if (json.getValue("regions") instanceof JsonArray) {
      java.util.ArrayList<org.eightlog.thumty.feature.FeatureRegion> list = new java.util.ArrayList<>();
      json.getJsonArray("regions").forEach( item -> {
        if (item instanceof JsonObject)
          list.add(new org.eightlog.thumty.feature.FeatureRegion((JsonObject)item));
      });
      obj.setRegions(list);
    }
    if (json.getValue("width") instanceof Number) {
      obj.setWidth(((Number)json.getValue("width")).intValue());
    }
  }

  public static void toJson(Features obj, JsonObject json) {
    json.put("empty", obj.isEmpty());
    json.put("height", obj.getHeight());
    if (obj.getRegions() != null) {
      json.put("regions", new JsonArray(
          obj.getRegions().
              stream().
              map(item -> item.toJson()).
              collect(java.util.stream.Collectors.toList())));
    }
    json.put("width", obj.getWidth());
  }
}