package org.eightlog.thumty.store.content;

import org.eightlog.thumty.store.AttributedByteStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Content extends AttributedByteStream {

    /**
     * Read file path
     * @return a file path
     */
    String getPath();

    /**
     * Read file attributes
     * @return a file attributes
     */
    @Override
    ContentAttributes getAttributes();
}
