package europeana.rnd.dataprocessing.dates.extraction;

public class Match {
	
	MatchId matchId;
	String input;
	String extracted;
	
	public Match(MatchId matchId, String input, String extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted;
	}
	
	public MatchId getMatchId() {
		return matchId;
	}
	public String getInput() {
		return input;
	}
	public String getExtracted() {
		return extracted;
	}

	public void setMatchId(MatchId matchId) {
		this.matchId=matchId;
	}

	public void setInput(String input) {
		this.input = input;
	}
	
}
