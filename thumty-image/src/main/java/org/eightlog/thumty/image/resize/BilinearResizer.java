package org.eightlog.thumty.image.resize;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class BilinearResizer extends AbstractResizer {

    private final Map<RenderingHints.Key, Object> hints;

    public BilinearResizer(@Nullable Rectangle sourceRegion) {
        this(sourceRegion, Collections.emptyMap());
    }

    public BilinearResizer(@Nullable Rectangle sourceRegion, Map<RenderingHints.Key, Object> hints) {
        super(sourceRegion);
        this.hints = ImmutableMap.<RenderingHints.Key, Object>builder()
                .putAll(hints)
                .put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                .build();
    }

    public BilinearResizer(Map<RenderingHints.Key, Object> hints) {
        this(null, hints);
    }

    public BilinearResizer() {
        this(null, Collections.emptyMap());
    }

    @Override
    protected Map<RenderingHints.Key, Object> getRenderingHints() {
        return hints;
    }
}
