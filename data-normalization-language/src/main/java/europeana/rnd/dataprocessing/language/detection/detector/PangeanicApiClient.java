package europeana.rnd.dataprocessing.language.detection.detector;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.TextInRecord.TaggedText;
import inescid.http.HttpRequest;
import inescid.http.HttpRequestService;
import inescid.http.UrlRequest;
import inescid.http.UrlRequest.HttpMethod;
import inescid.util.AccessException;

public class PangeanicApiClient extends Detector {
	String baseUrl="http://prod.pangeamt.com:8080/NexRelay/v1/detect_language";
	String apiKey="APIKEY";
	HttpRequestService httpService;
	
	public PangeanicApiClient() {
		super("Pangeanic");
		httpService=new HttpRequestService();
		httpService.init();
	}
	
	@Override
	public Result detectLanguage(String text) throws AccessException {
		try {
			UrlRequest urlRequestSettings = new UrlRequest(baseUrl, HttpMethod.POST);
			HttpEntity entity = new ByteArrayEntity(createJsonRequest(text).toString().getBytes("UTF-8"), ContentType.APPLICATION_JSON);
			urlRequestSettings.setRequestContent(entity);
			HttpRequest httpReq= new HttpRequest(urlRequestSettings);
			httpService.fetch(httpReq);
			String response = httpReq.getResponseContentAsString();
			if(httpReq.getResponseStatusCode()!=200)
				throw new AccessException(baseUrl, httpReq.getResponseStatusCode(), response);
//			System.out.println(response);
			JsonParser parser = Json.createParser(new StringReader(response));
			parser.next();
			JsonObject next = parser.getObject();
			Result res=new Result();
			JsonArray detectedLangs = next.getJsonArray("detected_langs");
			for(JsonValue langVal: detectedLangs) {
				JsonObject dtcLang = langVal.asJsonObject();
				String detectedLangCode = dtcLang.getString("src_detected");
				if(detectedLangCode!=null && !detectedLangCode.equals("N.A."))
					res.add(detectedLangCode, (float)dtcLang.getJsonNumber("src_lang_score").doubleValue()) ;
			}
			return res;
		} catch (InterruptedException | IOException e) {
			throw new AccessException(baseUrl, e);
		}
	}
	
	private JsonObject createJsonRequest(String text) {
		JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("apikey", Json.createValue(apiKey));
		JsonArrayBuilder textArray = Json.createArrayBuilder();
		textArray.add(text);
		ret.add("src", textArray.build());
		ret.add("mode", "EUROPEANA");
		return ret.build();
	}
	
	
	public static void main(String[] args) throws Exception {
		PangeanicApiClient cli=new PangeanicApiClient();
		Result detectLanguage = cli.detectLanguage("ISto é um teste da API da Pangeanic.");
		System.out.println(detectLanguage);
		
	}
}
