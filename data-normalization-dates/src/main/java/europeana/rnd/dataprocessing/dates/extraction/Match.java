package europeana.rnd.dataprocessing.dates.extraction;

public class Match {
	
	MatchId matchId;
	CleanId cleanOperation;
	String input;
	String extracted;
	
	public Match(MatchId matchId, String input, String extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted;
	}
	public Match(MatchId matchId, CleanId cleanOperation, String input, String extracted) {
		super();
		this.matchId = matchId;
		this.cleanOperation = cleanOperation;
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
		this.cleanOperation=result.getCleanOperation();
		this.extracted=result.getExtracted();
	}

	public CleanId getCleanOperation() {
		return cleanOperation;
	}

	public void setCleanOperation(CleanId cleanOperation) {
		this.cleanOperation = cleanOperation;
	}
	
}
