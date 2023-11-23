package europeana.rnd.dataprocessing.language.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {
	public static class DetectedLanguage {
		String langCode;
		float confidence;
		public String getLangCode() {
			return langCode;
		}
		public void setLangCode(String langCode) {
			this.langCode = langCode;
		}
		public float getConfidence() {
			return confidence;
		}
		public void setConfidence(float confidence) {
			this.confidence = confidence;
		}
		public DetectedLanguage(String langCode, float confidence) {
			super();
			this.langCode = langCode;
			this.confidence = confidence;
		}
		@Override
		public String toString() {
			return "[" + langCode + ", " + confidence + "]";
		}
		
	}
	
	List<DetectedLanguage> detected=new ArrayList<Result.DetectedLanguage>();

	public void add(String langCode, float confidence) {
		detected.add(new DetectedLanguage(langCode, confidence));
	}

	public List<DetectedLanguage> getDetected() {
		return Collections.unmodifiableList(detected);
	}

	@Override
	public String toString() {
		return "[detected=" + detected + "]";
	}

	public DetectedLanguage getFirstResult() {
		return detected.isEmpty() ? null : detected.get(0);
	}
	
	
}
