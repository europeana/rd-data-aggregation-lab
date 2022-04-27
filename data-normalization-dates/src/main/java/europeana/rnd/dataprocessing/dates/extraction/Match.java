package europeana.rnd.dataprocessing.dates.extraction;

import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;
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
	public boolean isCompleteDate() {
		if(extracted==null || extracted.isTimeOnly() )
			return false;
		if(extracted instanceof Instant) {
			return ((Instant)extracted).getDate().getDay()!=null;
		} else {
			Interval interval=(Interval)extracted;
			if (interval.getStart()!=null && interval.getEnd()!=null) {
				if(interval.getStart().getDate().isUnkown() || interval.getStart().getDate().isUnspecified() )
					return false;
				if(interval.getEnd().getDate().isUnkown() || interval.getEnd().getDate().isUnspecified() )
					return false;
				if(interval.getStart().getDate().getYearPrecision()!=null || interval.getEnd().getDate().getYearPrecision()!=null)
					return false;
				if(interval.getStart().getDate().getDay()!=null && interval.getStart().getDate().getDay()!=null)
					return true;
				if(interval.getStart().getDate().getMonth()==null && interval.getStart().getDate().getMonth()==null)
					return true;
			}
			return false;
		}
	}
		
}
