package org.eightlog.thumty.loader.fs;

import io.vertx.core.file.AsyncFile;
import org.eightlog.thumty.common.file.AsyncFileReadStream;
import org.eightlog.thumty.store.Attributes;
import org.eightlog.thumty.store.ExpirableAttributedContent;

import java.time.LocalDateTime;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class AttributedAsyncFileReadStream extends AsyncFileReadStream implements ExpirableAttributedContent {

    private final LocalDateTime expires;
    private final Attributes attributes;

    public AttributedAsyncFileReadStream(AsyncFile file, Attributes attributes, LocalDateTime expires) {
        super(file);
        this.expires = expires;
        this.attributes = attributes;
    }

    @Override
    public LocalDateTime getExpires() {
        return expires;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }
}
