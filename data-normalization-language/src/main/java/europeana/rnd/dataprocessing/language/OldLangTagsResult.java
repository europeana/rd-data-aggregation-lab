package europeana.rnd.dataprocessing.language;

import java.io.Serializable;

import inescid.util.datastruct.MapOfInts;

public class OldLangTagsResult implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum IN {CHO, CONTEXT};
	public enum SOURCE {PROVIDER, EUROPEANA};
	
	MapOfInts<String> tagsInProvider;
	MapOfInts<String> tagsInEuropeana;

	MapOfInts<String> tagsInProviderContext;
	MapOfInts<String> tagsInEuropeanaContext;

	public OldLangTagsResult() {
		tagsInProvider=new MapOfInts<String>();
		tagsInEuropeana=new MapOfInts<String>();
		tagsInProviderContext=new MapOfInts<String>();
		tagsInEuropeanaContext=new MapOfInts<String>();
	}

	public void inc(SOURCE source, String lang, IN in) {
		if(source==SOURCE.EUROPEANA)
			if(in==IN.CHO)
				tagsInEuropeana.incrementTo(lang);
			else
				tagsInEuropeanaContext.incrementTo(lang);
		else
			if(in==IN.CHO)
				tagsInProvider.incrementTo(lang);
			else
				tagsInProviderContext.incrementTo(lang);
	}

	public void add(OldLangTagsResult langsResult) {
		tagsInProvider.addToAll(langsResult.tagsInProvider);
		tagsInEuropeana.addToAll(langsResult.tagsInEuropeana);
		tagsInProviderContext.addToAll(langsResult.tagsInProviderContext);
		tagsInEuropeanaContext.addToAll(langsResult.tagsInEuropeanaContext);
	}

	public MapOfInts<String> getTagsInProvider() {
		return tagsInProvider;
	}

	public MapOfInts<String> getTagsInEuropeana() {
		return tagsInEuropeana;
	}

	public MapOfInts<String> getTagsInProviderContext() {
		return tagsInProviderContext;
	}

	public MapOfInts<String> getTagsInEuropeanaContext() {
		return tagsInEuropeanaContext;
	}

}
