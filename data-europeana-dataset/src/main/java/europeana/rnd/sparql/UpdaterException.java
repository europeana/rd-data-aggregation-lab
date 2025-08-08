package europeana.rnd.sparql;

/**
 * Base exception for the updater application
 */
public class UpdaterException extends Exception {

    /**
     * General error thrown by the sitemap application
     * @param s error message
     */
    public UpdaterException(String s) {
        super(s);
    }

    /**
     * General error thrown by the sitemap application
     * @param s error message
     * @param t throwable that caused the exception
     */
    public UpdaterException(String s, Throwable t) {
        super(s, t);
    }

}
