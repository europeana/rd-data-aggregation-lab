package europeana.rnd.dataprocessing.dates.edtf;

public class Interval extends TemporalEntity {
	
	Instant start;
	Instant end;
	
	public Interval(Instant start, Instant end) {
		super();
		this.start = start;
		this.end = end;
	}

	@Override
	public boolean isTimeOnly() {
		return (start==null || start.isTimeOnly() ) && (end==null || end.isTimeOnly() );
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


	@Override
	public void setApproximate(boolean approx) {
		if(start!=null && start.date!=null)
			start.date.setApproximate(approx);
		if(end!=null && end.date!=null)
			end.date.setApproximate(approx);
	}

	@Override
	public void setUncertain(boolean uncertain) {
		if(start!=null && start.date!=null)
			start.date.setUncertain(uncertain);
		if(end!=null && end.date!=null)
			end.date.setUncertain(uncertain);
	}
	
}
