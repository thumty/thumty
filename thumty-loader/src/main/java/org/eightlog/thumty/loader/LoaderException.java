package org.eightlog.thumty.loader;

/**
 * @author <a href="mailto:iliya.gr@gmail.com">Iliya Grushevskiy</a>
 */
public class LoaderException extends Exception {

    public LoaderException() {
    }

    public LoaderException(String message) {
        super(message);
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoaderException(Throwable cause) {
        super(cause);
    }
}
