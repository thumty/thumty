package org.eightlog.thumty.image.resize;

import com.twelvemonkeys.image.ResampleOp;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class LanczosResizer extends AbstractResizer {

    public LanczosResizer() {
        this(null);
    }

    public LanczosResizer(@Nullable Rectangle sourceRegion) {
        super(sourceRegion);
    }

    @Override
    public BufferedImage resize(BufferedImage src, BufferedImage dst) {
        return new ResampleOp(dst.getWidth(), dst.getHeight(), ResampleOp.FILTER_LANCZOS).filter(getSourceImage(src), dst);
    }

    @Override
    protected Map<RenderingHints.Key, Object> getRenderingHints() {
        return Collections.emptyMap();
    }
}
