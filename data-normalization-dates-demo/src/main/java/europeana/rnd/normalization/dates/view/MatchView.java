package europeana.rnd.normalization.dates.view;

import europeana.rnd.dataprocessing.dates.extraction.Match;

public class MatchView {
	Match match;

	public MatchView(Match match) {
		super();
		this.match = match;
	}
	
	public String getExtracted() {
		if (match==null) 
			return "";
		
	}

}
