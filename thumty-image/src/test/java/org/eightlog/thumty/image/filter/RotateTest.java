package org.eightlog.thumty.image.filter;

import org.junit.Test;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class RotateTest {

    @Test
    public void shouldRotateImage() throws Exception {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(45), 50, 50);
        System.err.println(transform.createTransformedShape(rectangle).getBounds());
    }
}