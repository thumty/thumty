package org.eightlog.thumty.store.content.jdbc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.binary.FSBinaryStore;
import org.eightlog.thumty.store.content.ContentStore;
import org.eightlog.thumty.store.descriptor.jdbc.JDBCDescriptorStoreProvider;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCContentStoreProvider {

    private final Vertx vertx;

    private final JDBCClient client;

    private final String basePath;

    public JDBCContentStoreProvider(Vertx vertx, JDBCClient client, String basePath) {
        this.vertx = vertx;
        this.client = client;
        this.basePath = basePath;
    }

    public Future<ContentStore> create(String bucket, JsonObject config) {
        JDBCWrapper jdbc = JDBCWrapper.create(client);

        String table = bucket + "_content";

        String binaryPath = basePath + "/" + bucket + "/content";
        String tmpPath = basePath + "/" + bucket + "/tmp";

        Future<Void> createTable = jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " " +
                "(path VARCHAR(8192) NOT NULL, " +
                "content_id VARCHAR(40), " +
                "sha1 VARCHAR(40), " +
                "size BIGINT, " +
                "content_type VARCHAR(1024), " +
                "meta VARCHAR(32768), " +
                "created TIMESTAMP," +
                "updated TIMESTAMP," +
                "PRIMARY KEY (path))"
        );

        return createTable
                .compose(v -> new JDBCDescriptorStoreProvider(vertx, client).createDescriptorStore(bucket, config))
                .map(descriptors -> new FSBinaryStore(vertx, descriptors, binaryPath, tmpPath))
                .map(binaries -> new JDBCContentStore(binaries, client, table));
    }
}
