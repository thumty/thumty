package org.eightlog.thumty.image.filter;

import com.jhlabs.image.SaturationFilter;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Saturation extends TranslateFilter {

    private final float amount;

    public Saturation(float amount) {
        if (amount < -1 || amount > 1) {
            throw new IllegalArgumentException(MessageFormat.format("Invalid saturation amount: {0}, must be in range [-1;1]", amount));
        }

        this.amount = amount;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        return new SaturationFilter(amount + 1).filter(image, image);
    }
}
