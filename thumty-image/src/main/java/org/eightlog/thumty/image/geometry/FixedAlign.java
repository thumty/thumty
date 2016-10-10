package org.eightlog.thumty.image.geometry;

import java.awt.*;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public enum FixedAlign implements Align {

    TOP () {
        @Override
        public Point calculate(Dimension source, Dimension target) {
            return new Point((source.width - target.width) / 2, 0);
        }
    },

    BOTTOM () {
        @Override
        public Point calculate(Dimension source, Dimension target) {
            return new Point((source.width - target.width) / 2, source.height - target.height);
        }
    },

    LEFT () {
        @Override
        public Point calculate(Dimension source, Dimension target) {
            return new Point(0, (source.height - target.height) / 2);
        }
    },

    RIGHT () {
        @Override
        public Point calculate(Dimension source, Dimension target) {
            return new Point(source.width - target.width, (source.height - target.height) / 2);
        }
    },

    CENTER () {
        @Override
        public Point calculate(Dimension source, Dimension target) {
            return new Point((source.width - target.width) / 2, (source.height - target.height) / 2);
        }
    }
}
