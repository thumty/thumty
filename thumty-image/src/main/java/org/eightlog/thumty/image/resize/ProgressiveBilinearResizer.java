package org.eightlog.thumty.image.resize;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class ProgressiveBilinearResizer extends BilinearResizer {

    public ProgressiveBilinearResizer(@Nullable Rectangle sourceRegion) {
        super(sourceRegion);
    }

    public ProgressiveBilinearResizer(@Nullable Rectangle sourceRegion, Map<RenderingHints.Key, Object> hints) {
        super(sourceRegion, hints);
    }

    public ProgressiveBilinearResizer(Map<RenderingHints.Key, Object> hints) {
        this(null, hints);
    }

    public ProgressiveBilinearResizer() {
        this(null, Collections.emptyMap());
    }

    @Override
    public BufferedImage resize(BufferedImage src, BufferedImage dst) {
        Rectangle region = getSourceRegion(src);

        int width = region.width;
        int height = region.height;

        int dstWidth = dst.getWidth();
        int dstHeight = dst.getHeight();

        int stepsV = (int) (Math.log(width / dstWidth) / Math.log(2));
        int stepsH = (int) (Math.log(height / dstHeight) / Math.log(2));

        int steps = Math.min(stepsV, stepsH);

        if (steps <= 1) {
            return super.resize(src, dst);
        }

        int tempWidth = (int) (dstWidth * Math.pow(2, steps));
        int tempHeight = (int) (dstHeight * Math.pow(2, steps));

        BufferedImage temp = new BufferedImage(tempWidth, tempHeight, src.getType());

        Graphics2D gTemp = temp.createGraphics();
        gTemp.setRenderingHints(getRenderingHints());
        gTemp.setComposite(AlphaComposite.Src);

        gTemp.drawImage(src,
                0, 0, tempWidth, tempHeight,
                region.x, region.y, region.x + width, region.y + height, null);

        for (int i = (int)Math.pow(2, steps - 1); i >= 1; i/=2) {
            gTemp.drawImage(temp,
                    0, 0, dstWidth * i, dstHeight * i,
                    0, 0, dstWidth * i * 2, dstHeight * i * 2, null);
        }

        gTemp.dispose();

        Graphics2D g = dst.createGraphics();
        g.drawImage(temp,
                0, 0, dstWidth, dstHeight,
                0, 0, dstWidth, dstHeight, null);
        g.dispose();

        return dst;
    }

}
