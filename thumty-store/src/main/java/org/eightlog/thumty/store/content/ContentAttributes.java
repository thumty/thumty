package org.eightlog.thumty.store.content;

import io.vertx.core.json.JsonObject;
import org.eightlog.thumty.store.Attributes;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ContentAttributes extends Attributes {

    private final LocalDateTime created;

    private final LocalDateTime updated;

    private final JsonObject meta;

    public ContentAttributes(Attributes attributes, LocalDateTime created, LocalDateTime updated, JsonObject meta) {
        this(attributes.getSha1(), attributes.getSize(), attributes.getSha1(), created, updated, meta);
    }

    public ContentAttributes(Attributes attributes, Instant created, Instant updated, String meta) {
        this(attributes.getSha1(), attributes.getSize(), attributes.getSha1(),
                created != null ? LocalDateTime.ofInstant(created, ZoneId.systemDefault()) : null,
                updated != null ? LocalDateTime.ofInstant(updated, ZoneId.systemDefault()) : null,
                meta != null ? new JsonObject(meta) : null);
    }

    public ContentAttributes(String sha1, long size, String contentType, Instant created, Instant updated, String meta) {
        this(sha1, size, contentType,
                created != null ? LocalDateTime.ofInstant(created, ZoneId.systemDefault()) : null,
                updated != null ? LocalDateTime.ofInstant(updated, ZoneId.systemDefault()) : null,
                meta != null ? new JsonObject(meta) : null);
    }

    public ContentAttributes(String sha1, long size, String contentType, LocalDateTime created, LocalDateTime updated, JsonObject meta) {
        super(sha1, size, contentType);
        this.created = created;
        this.updated = updated;
        this.meta = meta;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    @Nullable
    public JsonObject getMeta() {
        return meta;
    }
}
