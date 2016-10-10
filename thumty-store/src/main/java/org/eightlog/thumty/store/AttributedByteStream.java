package org.eightlog.thumty.store;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface AttributedByteStream extends ReadStream<Buffer> {

    /**
     * Read binary content attributes
     *
     * @return a binary content attributes
     */
    Attributes getAttributes();
}
