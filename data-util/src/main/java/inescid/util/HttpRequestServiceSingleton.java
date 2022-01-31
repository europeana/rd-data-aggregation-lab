package inescid.util;

import inescid.http.HttpRequestService;

public class HttpRequestServiceSingleton {
	private static HttpRequestService httpRequestService=null;

	public static HttpRequestService getHttpRequestService() {
		if(httpRequestService==null) {
			httpRequestService=new HttpRequestService();
			httpRequestService.init();
		}
		return httpRequestService;
	}

}
