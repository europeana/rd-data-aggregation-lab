package europeana.rnd.dataprocessing.language.detection.detector;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import europeana.rnd.dataprocessing.language.detection.Result;
import inescid.util.AccessException;

public class TikaLanguageDetector extends Detector {
	LanguageDetector langDetector;
	float minConfidence = 0;

	public TikaLanguageDetector(float minConfidence) throws IOException {
		super("ApacheTika");
		this.minConfidence = minConfidence;
		langDetector = LanguageDetector.getDefaultLanguageDetector();
		langDetector.loadModels();
	}

	@Override
	public Result detectLanguage(String text) throws AccessException {
		LanguageResult detect = langDetector.detect(text);
		Result res = new Result();
		if ((minConfidence == 0 || detect.getRawScore() >= minConfidence) && !StringUtils.isEmpty(detect.getLanguage()))
			res.add(detect.getLanguage(), detect.getRawScore());
		return res;
	}

	public static void main(String[] args) throws Exception {
		TikaLanguageDetector cli = new TikaLanguageDetector(0);
		Result detectLanguage = cli.detectLanguage("Isto é um teste da API do Apache Tika.");
		System.out.println(detectLanguage);
	}
}
