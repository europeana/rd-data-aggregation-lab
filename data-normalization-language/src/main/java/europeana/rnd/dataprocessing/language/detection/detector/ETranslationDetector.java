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

import eu.europeana.research.etranslation.langdetection.LangDetectResponse;
import eu.europeana.research.etranslation.langdetection.LanguageDetectionClient;
import eu.europeana.research.etranslation.langdetection.LanguageDetectionErrorCodeException;
import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.TextInRecord.TaggedText;
import inescid.http.HttpRequest;
import inescid.http.HttpRequestService;
import inescid.http.UrlRequest;
import inescid.http.UrlRequest.HttpMethod;
import inescid.util.AccessException;

public class ETranslationDetector extends Detector {
	LanguageDetectionClient client;
	
	public ETranslationDetector() {
		super("eTranslation");
		client=new LanguageDetectionClient();
	}
	
	@Override
	public Result detectLanguage(String text) throws AccessException {
		try {
			Result res=new Result();
			LangDetectResponse response = client.sendRequest(text);
			if(!response.getDetected().isEmpty()) {
				response.getDetected().forEach(lang -> {
					res.add(lang.getLangCode(), lang.getConfidence());
				});
			}
			return res;
		} catch (LanguageDetectionErrorCodeException e) {
			if(e.getErrorCode()==-70000) //language could not be detected
				return new Result();
			if(e.getErrorCode()==-20010) //invalid mime type (for some strange reason eTranslation returns this error for some strings)
				return new Result();
			throw new AccessException("eTranslation error", e);
		} catch (InterruptedException | IOException e) {
			throw new AccessException("eTranslation error", e);
		} catch (eu.europeana.research.etranslation.util.AccessException e) {
			throw new AccessException("eTranslation error: "+e.getCode(), e);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		ETranslationDetector cli=new ETranslationDetector();
//		Result detectLanguage = cli.detectLanguage("Uma frase em português"
//						Result detectLanguage = cli.detectLanguage("The frelders"
				Result detectLanguage = cli.detectLanguage("The free-holders... (4th impr.) / To which are added observations upon forms of government... By the learned Sir Robert Filmer,..."
//						+ " (4th impr.) "
//						+ "/ To which are added observations upon forms of government... By the learned Sir Robert Filmer,..."
				);
		System.out.println(detectLanguage);
		
	}
}
