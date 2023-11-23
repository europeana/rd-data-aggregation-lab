package europeana.rnd.dataprocessing.language.detection.detector;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;

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
import inescid.util.language.EuropeanLanguagesNal;
import inescid.util.language.NalLanguage;

public class ChatGptApiClient extends Detector {
	private static final int intervalBetweenRequestsInSeconds=22;
	private static String promptContext;
	static {
		StringBuffer sb=new StringBuffer();
		sb.append("The problem to solve is to detect the language in which pieces of text have been written. Use the following languages:\n");
		EuropeanLanguagesNal nal=new EuropeanLanguagesNal();
		for(NalLanguage lang: nal.getOfficialEuLanguages()) 
			sb.append(" - ").append(lang.getIso6391()).append("\n");
//				sb.append(" Detect the language in the following texts and give the answer in JSON format with the text as the key and the predicted language as the value. If not detected the language use 'None' as the value. Always return the same length for the dictionary as the list of the input text.\n");
		sb.append("Detect the language in the following text and give the answer in text format with the the predicted language as the value. If the language is not detected, use 'None' as value.\n\n");
		promptContext=sb.toString();
	}
	
	String baseUrl="https://api.openai.com/v1/chat/completions";
	String apiKey="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";
	String model = "gpt-3.5-turbo";
	HttpRequestService httpService;
	Instant lastRequestTs;
	
	public ChatGptApiClient() {
		super("ChatGPT");
		httpService=new HttpRequestService();
		httpService.init();
	}
	
	@Override
	public Result detectLanguage(String text) throws AccessException {
		try {
			slowDownRequests();			
			UrlRequest urlRequestSettings = new UrlRequest(baseUrl, HttpMethod.POST);
	    	urlRequestSettings.addHttpHeader("Authorization", "Bearer " + apiKey);
			String requestJsonString = createJsonRequest(promptContext+text).toString();
//			System.out.println(requestJsonString);
			HttpEntity entity = new ByteArrayEntity(requestJsonString.getBytes("UTF-8"), ContentType.APPLICATION_JSON);
			urlRequestSettings.setRequestContent(entity);
			urlRequestSettings.setConnectionTimeoutMs(5000);
			urlRequestSettings.setSocketTimeoutMs(5000);
			lastRequestTs=Instant.now();
			HttpRequest httpReq= new HttpRequest(urlRequestSettings);
			httpService.fetch(httpReq);
			String response = httpReq.getResponseContentAsString();
			if(httpReq.getResponseStatusCode()!=200) 
				throw new AccessException(baseUrl, httpReq.getResponseStatusCode(), response);
//			System.out.println(response);
//			System.out.println(extractMessageFromJSONResponse(response));
			Result res=new Result();
			String langCode=extractMessageFromJSONResponse(response);
			if(langCode.length()==2) 
				res.add(langCode, 1) ;
			return res;
		} catch (InterruptedException | IOException e) {
			throw new AccessException(baseUrl, e);
		}
	}
	
	private void slowDownRequests() {
		if(lastRequestTs==null)
			return;
		try {
			Instant now=Instant.now();
			Duration d=Duration.between(lastRequestTs, now);
			if(d.getSeconds()<intervalBetweenRequestsInSeconds) {
				System.out.println(Instant.now()+": ChatGPT pacing down... "+(intervalBetweenRequestsInSeconds-d.getSeconds())+" secs.");
				Thread.sleep((intervalBetweenRequestsInSeconds-d.getSeconds())*1000);
			}
		} catch (InterruptedException e) {
			//exit normaly
		}
	}

	private JsonObject createJsonRequest(String text) {
//        String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
        JsonObjectBuilder ret=Json.createObjectBuilder();
		ret.add("model", Json.createValue(model));
		JsonArrayBuilder messages = Json.createArrayBuilder();
		JsonObjectBuilder msg=Json.createObjectBuilder();
		msg.add("role", Json.createValue("user"));
		msg.add("content", Json.createValue(text));
		messages.add(msg.build());
		ret.add("messages", messages.build());
		return ret.build();
	}
	
	   public static String extractMessageFromJSONResponse(String response) {
	       int start = response.indexOf("content")+ 11;
	       int end = response.indexOf("\"", start);
	       return response.substring(start, end);
	   }
	
	
	
	public static void main(String[] args) throws Exception {
		try {
			ChatGptApiClient cli=new ChatGptApiClient();
			Result detectLanguage = cli.detectLanguage("ISto é um teste da API da Pangeanic.");
			System.out.println(detectLanguage);
		} catch (AccessException e) {
			System.out.println(e.getMessage());
			System.out.println(e.getAddress());
			System.out.println(e.getCode());
			System.out.println(e.getResponse());
			e.printStackTrace(System.out);
		}
		
	}
}
