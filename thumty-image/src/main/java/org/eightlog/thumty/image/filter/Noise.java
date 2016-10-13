package org.eightlog.thumty.image.filter;

import com.jhlabs.image.NoiseFilter;

import java.awt.image.BufferedImage;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Noise extends TranslateFilter {

    private final float amount;

    private final Distribution distribution;

    private final float density;

    private final boolean monochrome;

    public Noise(float amount, Distribution distribution, float density, boolean monochrome) {
        this.amount = amount;
        this.distribution = distribution;
        this.density = density;
        this.monochrome = monochrome;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        NoiseFilter filter = new NoiseFilter();
        filter.setAmount((int)(amount * 100));
        filter.setDistribution(distribution == Distribution.UNIFORM ? NoiseFilter.UNIFORM : NoiseFilter.GAUSSIAN);
        filter.setDensity(density);
        filter.setMonochrome(monochrome);
        return filter.filter(image, null);
    }

    public enum Distribution {
        UNIFORM, GAUSSIAN
    }
}
