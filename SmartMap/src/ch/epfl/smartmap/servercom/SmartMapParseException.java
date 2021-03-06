package ch.epfl.smartmap.servercom;

/**
 * An exception to be thrown when server's answers violate the required
 * format.
 * 
 * @author marion-S
 * @author Pamoi (code reviewed : 9.11.2014)
 */
public class SmartMapParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public SmartMapParseException() {
        super();
    }

    public SmartMapParseException(String message) {
        super(message);
    }

    public SmartMapParseException(Throwable throwable) {
        super(throwable);
    }
}
