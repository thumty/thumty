package org.eightlog.thumty.store.content.jdbc;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.jdbc.JDBCClient;
import org.eightlog.thumty.common.jdbc.JDBCTransaction;
import org.eightlog.thumty.common.jdbc.JDBCWrapper;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.binary.Binary;
import org.eightlog.thumty.store.binary.BinaryStore;
import org.eightlog.thumty.store.content.BinaryContent;
import org.eightlog.thumty.store.content.Content;
import org.eightlog.thumty.store.content.ContentAttributes;
import org.eightlog.thumty.store.content.ContentStore;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * JDBC backend file store
 * <p>
 * DB table structure
 * CREATE TABLE IF NOT EXISTS TABLE_NAME
 * (path VARCHAR(8192) NOT NULL,
 * content_id VARCHAR(40),
 * sha1 VARCHAR(40),
 * size BIGINT,
 * content_type VARCHAR(1024),
 * expires TIMESTAMP,
 * meta CLOB(32768),
 * PRIMARY KEY (path));
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class JDBCContentStore implements ContentStore {

    private final JDBCWrapper client;

    private final BinaryStore content;

    private final String table;

    public JDBCContentStore(BinaryStore content, JDBCClient client, String table) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(table, "table");

        this.client = JDBCWrapper.create(client);
        this.content = content;
        this.table = table;
    }

    @Override
    public void write(String path, @Nullable JsonObject meta, ReadStream<Buffer> stream, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(handler, "handler");

        stream.pause();

        content.create(stream)
                .compose(binary ->
                        client.begin(tx ->
                                delete(tx, path)
                                        .compose(done ->
                                                tx.update("INSERT INTO " + table + "(content_id, sha1, size, content_type, meta, created, updated, path) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)",
                                                        binary.getId(), binary.getAttributes().getSha1(), binary.getAttributes().getSize(), binary.getAttributes().getContentType(), meta != null ? meta.toString() : null, path)
                                                        .compose(v -> attributes(tx, path))
                                                        .map(attr -> record(binary, path, attr))
                                        )
                                        .compose(tx::commit)
                        )
                ).setHandler(handler);
    }

    @Override
    public void read(String path, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        client.query("SELECT content_id, sha1, size, content_type, created, updated, meta FROM " + table + " WHERE path = ? LIMIT 1", path)
                .compose(res -> {
                    if (res.getNumRows() > 0) {
                        JsonArray row = res.getResults().get(0);
                        String contentId = row.getString(0);

                        ContentAttributes attributes = new ContentAttributes(
                                row.getString(1),  // sha1
                                row.getLong(2),    // size
                                row.getString(3),  // content_type
                                row.getInstant(4), // created
                                row.getInstant(5), // updated
                                row.getString(6)   // meta
                        );

                        return content.get(contentId).map(binary -> record(binary, path, attributes));
                    } else {
                        return Future.failedFuture(new NotFoundException("File at path " + path + " doesn't exists"));
                    }
                }).setHandler(handler);
    }

    @Override
    public void exists(String path, Handler<AsyncResult<Boolean>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx -> exists(tx, path)).setHandler(handler);
    }

    @Override
    public void delete(String path, Handler<AsyncResult<Void>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx ->
                tx.query("SELECT content_id FROM " + table + " WHERE path = ? LIMIT 1", path)
                        .compose(res -> {
                            if (res.getNumRows() > 0) {
                                return Future.succeededFuture(res.getResults().get(0).getString(0));
                            } else {
                                return Future.failedFuture(new NotFoundException());
                            }
                        })
                        .compose(content::delete)
                        .compose(v -> tx.update("DELETE FROM " + table + " WHERE path = ?", path).map((Void) null))
                        .compose(tx::commit)
        ).setHandler(handler);
    }

    @Override
    public void copy(String from, String to, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(handler, "handler");

        if (!Objects.equals(from, to)) {
            client.begin(tx ->
                    exists(tx, from)
                            .compose(assertTrue(new NotFoundException("Content at path = " + from + " doesn't exists")))
                            .compose(v -> delete(tx, to))
                            .compose(v -> tx.query("SELECT content_id, sha1, size, content_type, meta FROM " + table + " WHERE path = ? LIMIT 1", from))
                            .compose(res -> {
                                JsonArray row = res.getResults().get(0);
                                String contentId = row.getString(0);    // content_id
                                String sha1 = row.getString(1);         // sha1
                                Long size = row.getLong(2);             // size
                                String contentType = row.getString(3);  // content_type
                                String meta = row.getString(4);         // meta

                                return content.duplicate(contentId)
                                        .compose(binary -> tx.update("INSERT INTO " + table + " (content_id, sha1, size, content_type, meta, created, updated, path) " +
                                                "VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)", binary.getId(), sha1, size, contentType, meta, to));
                            })
                            .compose(tx::commit)
            ).compose(v -> read(to)).setHandler(handler);
        }
    }

    @Override
    public void copy(String from, String to, JsonObject meta, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(handler, "handler");

        if (!Objects.equals(from, to)) {
            client.begin(tx ->
                    exists(tx, from)
                            .compose(assertTrue(new NotFoundException("File at path = " + from + " doesn't exists")))
                            .compose(v -> delete(tx, to))
                            .compose(v -> tx.query("SELECT content_id, sha1, size, content_type FROM " + table + " WHERE path = ? LIMIT 1", from))
                            .compose(res -> {
                                JsonArray row = res.getResults().get(0);
                                String contentId = row.getString(0);    // content_id
                                String sha1 = row.getString(1);         // sha1
                                Long size = row.getLong(2);             // size
                                String contentType = row.getString(3);  // content_type

                                return content.duplicate(contentId)
                                        .compose(binary -> tx.update("INSERT INTO " + table + " (content_id, sha1, size, content_type, meta, created, updated, path) " +
                                                "VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)", binary.getId(), sha1, size, contentType, meta != null ? meta.toString() : null, to));
                            })
                            .compose(tx::commit)
            ).compose(v -> read(to)).setHandler(handler);
        }
    }

    @Override
    public void attributes(String path, Handler<AsyncResult<ContentAttributes>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        client.begin(tx -> attributes(tx, path)).setHandler(handler);
    }

    /**
     * Check whether given path exists
     *
     * @param tx   the transaction
     * @param path the path to check
     * @return a boolean future
     */
    private Future<Boolean> exists(JDBCTransaction tx, String path) {
        return tx.query("SELECT 1 FROM " + table + " WHERE path = ?", path)
                .compose(res -> Future.succeededFuture(res.getNumRows() > 0));
    }

    /**
     * Delete content at path if it exists
     *
     * @param tx   the transaction
     * @param path the path
     * @return a result future
     */
    private Future<Void> delete(JDBCTransaction tx, String path) {
        return tx.query("SELECT content_id FROM " + table + " WHERE path = ?", path)
                .compose(res -> {
                    if (res.getNumRows() > 0) {
                        String id = res.getResults().get(0).getString(0);
                        return content.delete(id);
                    }
                    return Future.succeededFuture();
                })
                .compose(res -> tx.update("DELETE FROM " + table + " WHERE path = ?", path).map((Void) null));
    }

    /**
     * Read content attributes, fail if path doesn't exists
     *
     * @param tx   the transaction
     * @param path the path
     * @return a attributes future
     */
    private Future<ContentAttributes> attributes(JDBCTransaction tx, String path) {
        return tx.query("SELECT sha1, size, content_type, created, updated, meta FROM " + table + " WHERE path = ? LIMIT 1", path)
                .compose(res -> {
                    if (res.getNumRows() > 0) {
                        JsonArray row = res.getResults().get(0);
                        return Future.succeededFuture(new ContentAttributes(
                                row.getString(0),  // sha1
                                row.getLong(1),    // size
                                row.getString(2),  // content_type
                                row.getInstant(3), // created
                                row.getInstant(4), // updated
                                row.getString(5)   // meta
                        ));
                    } else {
                        return Future.failedFuture(new NotFoundException("File at path " + path + " doesn't exists"));
                    }
                });
    }

    /**
     * Build function that, on false return failed future
     *
     * @param throwable the failed future error
     * @return an assert function
     */
    private Function<Boolean, Future<Void>> assertTrue(Throwable throwable) {
        return res -> {
            if (res) {
                return Future.succeededFuture();
            }
            return Future.failedFuture(throwable);
        };
    }

    private Content record(Binary binary, String path, ContentAttributes attributes) {
        return new BinaryContent(binary, path, attributes);
    }

}
