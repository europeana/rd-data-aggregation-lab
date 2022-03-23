package europeana.rnd.dataprocessing.language;

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
	
	public Match(String input) {
		this.input = input;		
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

	public void setExtracted(String extracted) {
		this.extracted = extracted;
	}

	public void setResult(Match result) {
		matchId=result.getMatchId();
		this.extracted=result.getExtracted();
	}
	@Override
	public String toString() {
		return "Match [matchId=" + matchId + ", input=" + input + ", extracted=" + extracted + "]";
	}
}
