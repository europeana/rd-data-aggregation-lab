package europeana.rnd.sparql;

/**
 * Thrown when there is an error downloading files from the FTP server
 */
public class DownloadException extends UpdaterException {


    public DownloadException(String msg, Throwable t) {
        super(msg, t);
    }

    public DownloadException(String msg) {
        super(msg);
    }


}
