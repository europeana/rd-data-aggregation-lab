package europeana.rnd.dataprocessing.dates.extraction;

import europeana.rnd.dataprocessing.dates.edtf.TemporalEntity;

public class Match {
	
	MatchId matchId;
	CleanId cleanOperation;
	String input;
	TemporalEntity extracted;
	
	public Match(MatchId matchId, String input, TemporalEntity extracted) {
		super();
		this.matchId = matchId;
		this.input = input;
		this.extracted = extracted;
	}
	public Match(MatchId matchId, CleanId cleanOperation, String input, TemporalEntity extracted) {
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
	public TemporalEntity getExtracted() {
		return extracted;
	}

	public void setMatchId(MatchId matchId) {
		this.matchId=matchId;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setExtracted(TemporalEntity extracted) {
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
	@Override
	public String toString() {
		return "Match [matchId=" + matchId + ", cleanOperation=" + cleanOperation + ", input=" + input + ", extracted="
				+ extracted + "]";
	}
	
	
	
}
