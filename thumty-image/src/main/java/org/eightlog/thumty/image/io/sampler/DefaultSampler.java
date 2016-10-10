package org.eightlog.thumty.image.io.sampler;

/**
 * The default image sampler.
 *
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class DefaultSampler implements ImageSampler {

    public final static DefaultSampler INSTANCE = new DefaultSampler();

    @Override
    public int getSampling(int width, int height) {
        return 1;
    }
}
