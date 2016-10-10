package org.eightlog.thumty.store.descriptor;

import org.eightlog.thumty.store.Attributes;

import java.io.Serializable;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class Descriptor extends Attributes implements Serializable {

    private final long chunkSize;
    private final long numberOfChunks;

    public Descriptor(String sha1, long size, String contentType) {
        this(sha1, size, 1, size, contentType);
    }

    public Descriptor(String sha1, long chunkSize, long numberOfChunks, long size, String contentType) {
        super(sha1, size, contentType);
        this.chunkSize = chunkSize;
        this.numberOfChunks = numberOfChunks;
    }
    /**
     * @return a content chunk size
     */
    public long getChunkSize() {
        return chunkSize;
    }

    /**
     * @return a content number of chunks
     */
    public long getNumberOfChunks() {
        return numberOfChunks;
    }
}
