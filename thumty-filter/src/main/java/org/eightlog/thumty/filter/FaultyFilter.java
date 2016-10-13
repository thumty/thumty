package org.eightlog.thumty.filter;

import io.vertx.core.Future;
import org.eightlog.thumty.image.Image;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FaultyFilter implements AsyncFilter {

    private final Throwable t;

    public FaultyFilter(Throwable t) {
        this.t = t;
    }

    public Future<Image> apply(Image image) {
        return Future.failedFuture(t);
    }
}
