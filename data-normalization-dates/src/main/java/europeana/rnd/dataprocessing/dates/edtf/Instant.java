package europeana.rnd.dataprocessing.dates.edtf;

public class Instant extends TemporalEntity {
	Date date;
	Time time;
	
	public Instant(Date date, Time time) {
		super();
		this.date = date;
		this.time = time;
	}
	
	public Instant(Date date) {
		super();
		this.date = date;
	}

	public Instant(Time parseTime) {
		this(null, parseTime);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}
	
	
	
	
}
