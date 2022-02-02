package inescid.util;

public interface Handler<TYPE, ERROR_TYPE> {
	public boolean handle(TYPE resource) throws Exception;
	public boolean handleError(ERROR_TYPE error, Exception e);
}
