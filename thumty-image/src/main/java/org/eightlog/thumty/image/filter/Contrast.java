package org.eightlog.thumty.image.filter;

import com.jhlabs.image.TransferFilter;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class Contrast extends TranslateFilter {

    private final float amount;

    public Contrast(float amount) {
        if (amount < -1 || amount > 1) {
            throw new IllegalArgumentException(MessageFormat.format("Invalid brightness amount: {0}, must be in range [-1;1]", amount));
        }
        this.amount = amount;
    }

    @Override
    protected BufferedImage apply(BufferedImage image) {
        return new TransferFilter() {
            @Override
            protected float transferFunction(float v) {
                return (v - 0.5f) * (amount + 1) + 0.5f;
            }
        }.filter(image, image);
    }
}
