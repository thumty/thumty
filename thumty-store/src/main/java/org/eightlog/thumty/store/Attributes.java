package org.eightlog.thumty.store;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * The content attributes class.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Attributes implements Serializable {

    private final String sha1;

    private final long size;

    private final String contentType;

    public Attributes(@Nullable String sha1, long size, @Nullable String contentType) {
        this.sha1 = sha1;
        this.size = size;
        this.contentType = contentType;
    }

    /**
     * @return a content sha1 hash string representation or null if sha1 is unknown
     */
    @Nullable
    public String getSha1() {
        return sha1;
    }

    /**
     * @return a content size or 0 if size is unknown
     */
    public long getSize() {
        return size;
    }

    /**
     * @return a content type or null if content type is unknown
     */
    @Nullable
    public String getContentType() {
        return contentType;
    }
}
