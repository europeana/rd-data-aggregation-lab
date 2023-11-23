package europeana.rnd.dataprocessing.language.detection.detector;

import europeana.rnd.dataprocessing.language.detection.Result;
import inescid.util.AccessException;

public abstract class Detector {
	String name;

	public Detector(String name) {
		this.name=name;
	}

	public String getName() {
		return name;
	}
	
	public abstract Result detectLanguage(String text) throws AccessException;
}
