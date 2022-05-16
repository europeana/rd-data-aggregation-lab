package europeana.rnd.dataprocessing.dates.edtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Year;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Instant extends TemporalEntity implements Serializable {
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

	@Override
	public boolean isTimeOnly() {
		return date==null;
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

	@Override
	public void setApproximate(boolean approx) {
		date.setApproximate(approx);
	}

	@Override
	public void setUncertain(boolean uncertain) {
		date.setUncertain(uncertain);
	}

	@Override
	public boolean isApproximate() {
		return date.isApproximate();
	}
	
	@Override
	public boolean isUncertain() {
		return date.isUncertain();
	}
	
	@Override
	public void switchDayMonth() {
		if(date!=null) 
			date.switchDayMonth();
	}
	
	@Override
	public TemporalEntity copy() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out=new ObjectOutputStream(bytes);
			out.writeObject(this);
			out.close();
			ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			Instant copy=(Instant)in.readObject();
			return copy;
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public Instant getFirstDay() {
		Instant firstDay=null;
		if(getDate()!=null) {
			if(!getDate().isUnkown() && !getDate().isUnspecified()) {
				firstDay=(Instant) this.copy();
				firstDay.setTime(null);
				if(getDate().getYear()>-9999 && getDate().getYear()<9999) {
					if (getDate().getMonth()!=null && getDate().getMonth()>0) 
						firstDay.getDate().setDay(1);
					else {
						firstDay.getDate().setMonth(1);
						firstDay.getDate().setDay(1);
					}
				}
			}
		}
		return firstDay;
	}
	
	@Override
	public Instant getLastDay() {
		Instant lastDay=null;
		if(getDate()!=null) {
			if(!getDate().isUnkown() && !getDate().isUnspecified()) {
				lastDay=(Instant) this.copy();
				lastDay.setTime(null);
				if(getDate().getYear()>-9999 && getDate().getYear()<9999) {
					if (getDate().getMonth()!=null && getDate().getMonth()>0) {
						if(EdtfValidator.isMonthOf31Days(getDate().getMonth()))
							lastDay.getDate().setDay(31);
						else if(getDate().getMonth()==2) {
							if(Year.isLeap(getDate().getYear()))
								lastDay.getDate().setDay(29);
							else
								lastDay.getDate().setDay(28);
						} else
							lastDay.getDate().setDay(30);
					} else {
						lastDay.getDate().setMonth(12);
						lastDay.getDate().setDay(31);
					}
				}
			}
		}
		return lastDay;
	}
	
	@Override
	public void removeTime() {
		time=null;
	}
}
