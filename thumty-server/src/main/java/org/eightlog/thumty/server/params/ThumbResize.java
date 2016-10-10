package org.eightlog.thumty.server.params;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public enum ThumbResize {

    FIT, FILL;

    public static boolean canParse(String text) {
        return text.equalsIgnoreCase("fit-in") || text.equalsIgnoreCase("fill");
    }

    public static ThumbResize parse(String text) {
        switch (text.toLowerCase()) {
            case "fit-in":
                return FIT;
            case "fill":
                return FILL;
        }

        throw new IllegalArgumentException("Invalid resize format");
    }

    @Override
    public String toString() {
        return this == FIT ? "fit-in" : "fill";
    }
}
