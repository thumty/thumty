package org.eightlog.thumty.filters;

/**
 * @author Iliya Grushevskiy <iliya.gr@gmail.com>
 */
public class FilterException extends Exception {
    public FilterException() {
    }

    public FilterException(String message) {
        super(message);
    }

    public FilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterException(Throwable cause) {
        super(cause);
    }
}
