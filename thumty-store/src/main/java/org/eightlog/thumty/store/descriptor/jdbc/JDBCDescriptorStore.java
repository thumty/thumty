package org.eightlog.thumty.store.descriptor.jdbc;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.common.jdbc.JDBCTransaction;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.descriptor.Descriptor;
import org.eightlog.thumty.store.descriptor.DescriptorStore;

import java.util.Objects;

/**
 * JDBC back-ended descriptor store.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCDescriptorStore implements DescriptorStore {

    public final static String CONFIG_TABLE_NAME = "table";
    private final static String DEFAULT_DB_TABLE = "descriptors";

    private JDBCWrapper client;

    private String table;

    public JDBCDescriptorStore(JDBCClient client, String table) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(table, "table");

        this.client = JDBCWrapper.create(client);
        this.table = table;
    }

    @Override
    public void read(String id, Handler<AsyncResult<Descriptor>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
            tx.query("SELECT sha1, chunk_size, number_of_chunks, size, content_type FROM " + table + " WHERE id = ? LIMIT 1", id)
                .compose(res -> {
                    if (res.getNumRows() != 0) {
                        JsonArray record = res.getResults().get(0);
                        return Future.succeededFuture(
                                new Descriptor(
                                        record.getString(0), // sha1
                                        record.getLong(1),   // chunkSize
                                        record.getLong(2),   // numberOfChunks
                                        record.getLong(3),   // size
                                        record.getString(4)  // contentType
                                )
                        );
                    } else {
                        return Future.failedFuture(new NotFoundException("Descriptor with id = " + id + " doesn't exists"));
                    }
                })
        ).setHandler(handler);
    }

    @Override
    public void create(String id, Descriptor descriptor, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
            tx.query("SELECT id FROM " + table + " WHERE id = ?", id)
                    .compose(res -> Future.succeededFuture(res.getNumRows() > 0))
                    .compose(exists -> {
                        if (exists) {
                            return tx.update("UPDATE " + table + " SET sha1 = ?, chunk_size = ?, number_of_chunks = ?, " +
                                            "number_of_references = number_of_references + 1, size = ?, content_type = ? WHERE id = ?",
                                    descriptor.getSha1(), descriptor.getChunkSize(), descriptor.getNumberOfChunks(),
                                    descriptor.getSize(), descriptor.getContentType(), id);
                        } else {
                            return tx.update("INSERT INTO " + table + " (sha1, chunk_size, number_of_chunks, number_of_references, " +
                                            "size, content_type, id) VALUES (?, ?, ?, 1, ?, ?, ?)",
                                    descriptor.getSha1(), descriptor.getChunkSize(), descriptor.getNumberOfChunks(),
                                    descriptor.getSize(), descriptor.getContentType(), id);
                        }
                    })
                    .compose(v -> getNumberOfReferences(tx, id))
                    .compose(tx::commit)
        ).setHandler(handler);
    }

    @Override
    public void delete(String id, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
                tx.update("UPDATE " + table + " SET number_of_references = number_of_references - 1 WHERE id = ?", id)
                .compose(v -> getNumberOfReferences(tx, id))
                .compose(n -> {
                    if (n == 0) {
                        return tx.update("DELETE FROM " + table + " WHERE id = ?", id).map(0L);
                    } else {
                        return Future.succeededFuture(n);
                    }
                })
                .compose(tx::commit)
        ).setHandler(handler);
    }

    @Override
    public void exists(String id, Handler<AsyncResult<Boolean>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
            tx.query("SELECT id FROM " + table + " WHERE id = ?", id)
                    .compose(res -> Future.succeededFuture(res.getNumRows() > 0))
        ).setHandler(handler);
    }

    @Override
    public void duplicate(String id, Handler<AsyncResult<Long>> handler) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
                tx.update("UPDATE " + table + " SET number_of_references = number_of_references + 1 WHERE id = ?", id)
                        .compose(v -> getNumberOfReferences(tx, id))
                        .compose(tx::commit)
        ).setHandler(handler);
    }

    private Future<Long> getNumberOfReferences(JDBCTransaction tx, String id) {
        return tx.query("SELECT number_of_references FROM " + table + " WHERE id = ?", id).map(res -> {
            if (res.getNumRows() > 0) {
                return res.getResults().get(0).getLong(0);
            } else {
                return -1L;
            }
        });
    }

}
