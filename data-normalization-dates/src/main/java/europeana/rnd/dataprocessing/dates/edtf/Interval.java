package europeana.rnd.dataprocessing.dates.edtf;

public class Interval extends TemporalEntity {
	
	Instant start;
	Instant end;
	
	public Interval(Instant start, Instant end) {
		super();
		this.start = start;
		this.end = end;
	}

	public Instant getStart() {
		return start;
	}

	public void setStart(Instant start) {
		this.start = start;
	}

	public Instant getEnd() {
		return end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}
	
	
}
