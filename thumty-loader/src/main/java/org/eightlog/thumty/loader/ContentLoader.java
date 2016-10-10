package org.eightlog.thumty.loader;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.eightlog.thumty.store.ExpirableAttributedContent;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public interface ContentLoader {

    /**
     * Check whether loader can load content from location
     *
     * @param location the content location
     * @return true if loader can load uri, false otherwise
     */
    boolean canLoadResource(String location);

    /**
     * Load content from location
     *
     * @param location the content location
     * @return a content future
     */
    default Future<ExpirableAttributedContent> load(String location) {
        Future<ExpirableAttributedContent> future = Future.future();
        load(location, future.completer());
        return future;
    }

    /**
     * Load content from location
     *
     * @param location the content location
     * @param handler  the result handler
     */
    void load(String location, Handler<AsyncResult<ExpirableAttributedContent>> handler);
}
