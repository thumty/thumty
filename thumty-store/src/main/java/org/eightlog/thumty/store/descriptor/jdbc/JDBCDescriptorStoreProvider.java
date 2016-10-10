package org.eightlog.thumty.store.descriptor.jdbc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.descriptor.DescriptorStore;
import org.eightlog.thumty.store.descriptor.DescriptorStoreProvider;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCDescriptorStoreProvider implements DescriptorStoreProvider{

    private final Vertx vertx;

    private final JDBCClient client;

    public JDBCDescriptorStoreProvider(Vertx vertx, JDBCClient client) {
        this.vertx = vertx;
        this.client = client;
    }

    @Override
    public Future<DescriptorStore> createDescriptorStore(String name, JsonObject config) {
        JDBCWrapper jdbc = JDBCWrapper.create(client);

        String table = name + "_descriptors";

        Future<Void> createTable = jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " " +
                "(id VARCHAR(128) NOT NULL, " +
                "sha1 VARCHAR(40), " +
                "chunk_size BIGINT, " +
                "number_of_chunks BIGINT, " +
                "number_of_references BIGINT, " +
                "size BIGINT, " +
                "content_type VARCHAR(1024), " +
                "PRIMARY KEY (id))");

        return createTable.map(v -> new JDBCDescriptorStore(client, table));
    }
}
