package org.eightlog.thumty.store;

import java.time.LocalDateTime;

/**
 * Represents content with attributes and expiration time.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ExpirableAttributedContent extends AttributedByteStream {

    /**
     * Read expiration time
     *
     * @return an expiration time
     */
    LocalDateTime getExpires();
}
