package org.eightlog.thumty.loader;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.eightlog.thumty.store.ExpirableAttributedContent;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class FaultyContentLoader implements ContentLoader {

    private final Throwable throwable;

    public FaultyContentLoader(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean canLoadResource(String location) {
        return false;
    }

    @Override
    public void load(String location, Handler<AsyncResult<ExpirableAttributedContent>> handler) {
        handler.handle(Future.failedFuture(throwable));
    }
}
