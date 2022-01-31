package europeana.rnd.dataprocessing.dates.edtf;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

	public Instant(java.util.Date javaDate) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(javaDate);
		date=new Date();
		date.setYear(cal.get(Calendar.YEAR));
		date.setMonth(cal.get(Calendar.MONTH));
		date.setDay(cal.get(Calendar.DAY_OF_MONTH));
		time=new Time();
		time.setHour(cal.get(Calendar.HOUR_OF_DAY));
		time.setMinute(cal.get(Calendar.MINUTE));
		time.setSecond(cal.get(Calendar.SECOND));
		time.setMillisecond(cal.get(Calendar.MILLISECOND));
		//TODO: convert timezone
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
