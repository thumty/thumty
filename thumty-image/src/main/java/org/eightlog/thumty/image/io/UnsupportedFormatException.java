package org.eightlog.thumty.image.io;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class UnsupportedFormatException extends Exception {

    public UnsupportedFormatException() {
    }

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFormatException(Throwable cause) {
        super(cause);
    }
}
