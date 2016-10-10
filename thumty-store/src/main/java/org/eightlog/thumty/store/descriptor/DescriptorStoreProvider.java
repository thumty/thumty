package org.eightlog.thumty.store.descriptor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface DescriptorStoreProvider {

    Future<DescriptorStore> createDescriptorStore(String name, JsonObject config);
}
