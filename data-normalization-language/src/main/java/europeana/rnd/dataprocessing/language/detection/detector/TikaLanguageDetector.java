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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import europeana.rnd.dataprocessing.language.LanguageAlpha3Code;
import europeana.rnd.dataprocessing.language.LanguageCode;
import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.TextInRecord.TaggedText;
import inescid.http.HttpRequest;
import inescid.http.HttpRequestService;
import inescid.http.UrlRequest;
import inescid.http.UrlRequest.HttpMethod;
import inescid.util.AccessException;

public class TikaLanguageDetector extends Detector {
	LanguageDetector langDetector;
	float minConfidence=0;
	
	public TikaLanguageDetector(float minConfidence) throws IOException {
		super("ApacheTika");
		this.minConfidence = minConfidence;
		langDetector=LanguageDetector.getDefaultLanguageDetector(); 
		langDetector.loadModels();
	}
	
	@Override
	public Result detectLanguage(String text) throws AccessException {
		LanguageResult detect = langDetector.detect(text);
		Result res=new Result();
//		res.add(LanguageAlpha3Code.valueOf(detect.getLanguage()).getAlpha2().name(), detect.getRawScore());
		if ((minConfidence==0 || detect.getRawScore()>=minConfidence) && !StringUtils.isEmpty(detect.getLanguage()))
			res.add(detect.getLanguage(), detect.getRawScore());
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		TikaLanguageDetector cli=new TikaLanguageDetector(0);
		Result detectLanguage = cli.detectLanguage("Isto é um teste da API do Apache Tika.");
		System.out.println(detectLanguage);
		
	}
}
