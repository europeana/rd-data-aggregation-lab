package europeana.rnd.dataprocessing.dates.edtf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Interval extends TemporalEntity implements Serializable {
	
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
	
	@Override
	public void switchDayMonth() {
		if (start!=null) 
			start.switchDayMonth();
		if (end!=null) 
			end.switchDayMonth();
	}
	
	@Override
	public TemporalEntity copy() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out=new ObjectOutputStream(bytes);
			out.writeObject(this);
			out.close();
			ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
			Interval copy=(Interval)in.readObject();
			return copy;
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
}
