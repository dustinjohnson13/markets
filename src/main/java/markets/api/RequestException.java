package markets.api;

public class RequestException extends Exception {

    public RequestException(String message, Throwable cause) {
        super(cause);
    }

}
