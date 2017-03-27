package org.eightlog.thumty.filter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.eightlog.thumty.image.Image;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public abstract class AbstractAsyncFilter implements AsyncFilter {

    private final Vertx vertx;

    public AbstractAsyncFilter(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Future<Image> apply(Image image) {
        Future<Image> future = Future.future();

        vertx.executeBlocking(f -> {
            try {
                f.complete(applyBlocking(image));
            } catch (Throwable t) {
                f.fail(t);
            }
        }, false, future.completer());

        return future;
    }

    @Override
    public AsyncFilter andThen(AsyncFilter after) {
        if (after instanceof AbstractAsyncFilter) {
            final AbstractAsyncFilter afterFilter = (AbstractAsyncFilter) after;

            return new AbstractAsyncFilter(vertx) {
                @Override
                protected Image applyBlocking(Image image) {
                    return afterFilter.applyBlocking(applyBlocking(image));
                }
            };

        }
        return image -> apply(image).compose(after::apply);
    }

    protected abstract Image applyBlocking(Image image);

    protected Vertx getVertx() {
        return vertx;
    }

}
