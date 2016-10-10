package org.eightlog.thumty.store.content;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import org.eightlog.thumty.common.mvstore.AsyncMVMap;
import org.eightlog.thumty.common.mvstore.AsyncMVStore;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.NotFoundException;
import org.eightlog.thumty.store.binary.Binary;
import org.eightlog.thumty.store.binary.BinaryStore;
import org.h2.mvstore.MVStore;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MVStore backend content store
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class MVStoreContentStore implements ContentStore {

    private final BinaryStore content;

    private final AsyncMVMap<String, Entry> table;

    public static Future<ContentStore> create(BinaryStore binaryStore, AsyncMVStore mvStore, String table) {
        return mvStore.<String, Entry>getMap(table).map(map -> new MVStoreContentStore(binaryStore, map));
    }

    public MVStoreContentStore(BinaryStore content, AsyncMVMap<String, Entry> table) {
        this.content = content;
        this.table = table;
    }

    public MVStoreContentStore(Vertx vertx, BinaryStore content, MVStore mvStore, String table) {
        this.content = content;
        this.table = new AsyncMVMap<>(vertx, mvStore, mvStore.openMap(table));
    }

    @Override
    public void write(String path, @Nullable JsonObject meta, ReadStream<Buffer> stream, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(handler, "handler");

        stream.pause();

        table.remove(path)
                .compose(entry -> {
                    if (entry != null) {
                        return content.delete(entry.getContentId());
                    } else {
                        return Future.succeededFuture();
                    }
                })
                .compose(v -> content.create(stream))
                .compose(ref -> {
                    LocalDateTime now = LocalDateTime.now();
                    Entry entry = new Entry(ref.getId(), ref.getAttributes(), meta, now, now, now);

                    return table.put(path, entry).map(record(ref, path, entry.toAttributes()));
                })
                .setHandler(handler);
    }

    @Override
    public void read(String path, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        table.get(path).compose(entry -> {
            if (entry != null) {
                return content.get(entry.getContentId()).map(b -> record(b, path, entry.toAttributes()));
            } else {
                return Future.failedFuture(new NotFoundException("Content at path " + path + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    @Override
    public void exists(String path, Handler<AsyncResult<Boolean>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        table.get(path).compose(entry ->{
            if (entry != null) {
                return Future.succeededFuture(true);
            } else {
                return Future.succeededFuture(false);
            }
        }).setHandler(handler);
    }

    @Override
    public void delete(String path, Handler<AsyncResult<Void>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        table.remove(path).compose(entry -> {
            if (entry != null) {
                return content.delete(path);
            } else {
                return Future.failedFuture(new NotFoundException("Content at path " + path + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    @Override
    public void copy(String from, String to, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(handler, "handler");

        if (!Objects.equals(from, to)) {
            table.get(from)
                    .compose(entry -> {
                        if (entry != null) {
                            return Future.succeededFuture(entry);
                        } else {
                            return Future.failedFuture(new NotFoundException("Content at path " + from + " doesn't exists"));
                        }
                    })
                    .compose(entry ->
                            table.remove(to)
                                    .compose(e -> {
                                        if (e != null) {
                                            return content.delete(entry.getContentId());
                                        } else {
                                            return Future.succeededFuture();
                                        }
                                    })
                                    .map(entry)
                    )
                    .compose(entry ->
                            content.duplicate(entry.getContentId())
                                    .compose(binary -> {
                                        LocalDateTime now = LocalDateTime.now();
                                        Entry e = new Entry(binary.getId(), binary.getAttributes(), entry.getMetaJson(), now, now, now);

                                        return table.put(to, e).map(record(binary, to, e.toAttributes()));
                                    })
                    )
                    .setHandler(handler);
        } else {
            handler.handle(Future.failedFuture(new NotFoundException("Content at path " + from + " doesn't exists")));
        }
    }

    @Override
    public void copy(String from, String to, JsonObject meta, Handler<AsyncResult<Content>> handler) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(handler, "handler");

        if (!Objects.equals(from, to)) {
            table.get(from)
                    .compose(entry -> {
                        if (entry != null) {
                            return Future.succeededFuture(entry);
                        } else {
                            return Future.failedFuture(new NotFoundException("Content at path " + from + " doesn't exists"));
                        }
                    })
                    .compose(entry ->
                            table.remove(to)
                                    .compose(e -> {
                                        if (e != null) {
                                            return content.delete(entry.getContentId());
                                        } else {
                                            return Future.succeededFuture();
                                        }
                                    })
                                    .map(entry)
                    )
                    .compose(entry ->
                            content.duplicate(entry.getContentId())
                                    .compose(binary -> {
                                        LocalDateTime now = LocalDateTime.now();
                                        Entry e = new Entry(binary.getId(), binary.getAttributes(), meta, now, now, now);
                                        return table.put(to, e).map(record(binary, to, e.toAttributes()));
                                    })
                    )
                    .setHandler(handler);
        } else {
            handler.handle(Future.failedFuture(new NotFoundException("Content at path " + from + " doesn't exists")));
        }
    }

    @Override
    public void attributes(String path, Handler<AsyncResult<ContentAttributes>> handler) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(handler, "handler");

        table.get(path).compose(entry -> {
            if (entry != null) {
                return Future.succeededFuture(entry.toAttributes());
            } else {
                return Future.failedFuture(new NotFoundException("Content at path " + path + " doesn't exists"));
            }
        }).setHandler(handler);
    }

    private Content record(Binary binary, String path, ContentAttributes attributes) {
        return new BinaryContent(binary, path, attributes);
    }

    private static class Entry implements Serializable {
        private final String contentId;
        private final String sha1;
        private final long size;
        private final String contentType;
        private final String meta;
        private final LocalDateTime created;
        private final LocalDateTime updated;

        public Entry(String contentId, Attributes attributes, JsonObject meta,
                     LocalDateTime created, LocalDateTime updated, LocalDateTime accessed) {
            this(contentId, attributes.getSha1(), attributes.getSize(), attributes.getContentType(),
                    meta != null ? meta.toString() : null, created, updated);
        }

        public Entry(String contentId, String sha1, long size, String contentType, String meta,
                     LocalDateTime created, LocalDateTime updated) {
            this.contentId = contentId;
            this.sha1 = sha1;
            this.size = size;
            this.contentType = contentType;
            this.meta = meta;
            this.created = created;
            this.updated = updated;
        }

        public String getContentId() {
            return contentId;
        }

        public String getSha1() {
            return sha1;
        }

        public long getSize() {
            return size;
        }

        public String getContentType() {
            return contentType;
        }

        public String getMeta() {
            return meta;
        }

        public LocalDateTime getCreated() {
            return created;
        }

        public LocalDateTime getUpdated() {
            return updated;
        }

        public JsonObject getMetaJson() {
            return meta != null ? new JsonObject(meta) : null;
        }

        public ContentAttributes toAttributes() {
            return new ContentAttributes(sha1, size, contentType, created, updated, getMetaJson());
        }
    }
}
