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
public class BlackmanSincResizer extends AbstractResizer {

    public BlackmanSincResizer() {
        this(null);
    }

    public BlackmanSincResizer(@Nullable Rectangle sourceRegion) {
        super(sourceRegion);
    }

    @Override
    public BufferedImage resize(BufferedImage src, BufferedImage dst) {
        return new ResampleOp(dst.getWidth(), dst.getHeight(), ResampleOp.FILTER_BLACKMAN_SINC).filter(getSourceImage(src), dst);
    }

    @Override
    protected Map<RenderingHints.Key, Object> getRenderingHints() {
        return Collections.emptyMap();
    }
}
