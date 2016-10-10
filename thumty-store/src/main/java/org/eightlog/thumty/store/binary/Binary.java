package org.eightlog.thumty.store.binary;

import org.eightlog.thumty.store.AttributedByteStream;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface Binary extends AttributedByteStream {

    /**
     * @return a binary content unique id
     */
    String getId();

}
