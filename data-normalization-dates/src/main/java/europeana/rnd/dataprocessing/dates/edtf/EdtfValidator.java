package europeana.rnd.dataprocessing.dates.edtf;

import java.time.Year;
import java.util.Calendar;

import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;

public class EdtfValidator {
	
	public static boolean validate(TemporalEntity edtf) {
		if(edtf instanceof Instant)
			return validateInstant((Instant) edtf);
	    return validateInterval((Interval)edtf);
	}

	private static boolean validateInterval(Interval edtf) {
		if( edtf.getStart()==null || edtf.getEnd()==null ||
				!validateInstant(edtf.getStart()) || !validateInstant(edtf.getEnd()))
			return false;
		
		Date sDate = edtf.getStart().getDate();
		Date eDate = edtf.getEnd().getDate();
		if((eDate.isUnkown() || eDate.isUnspecified()) && (sDate.isUnkown() || sDate.isUnspecified()))
			return false;
		if((eDate.isUnkown() || eDate.isUnspecified()) && !(sDate.isUnkown() || sDate.isUnspecified()))
			return true;
		if((sDate.isUnkown() || sDate.isUnspecified()) && !(eDate.isUnkown() || eDate.isUnspecified()))
			return true;
		
		if(sDate.yearPrecision==null && eDate.yearPrecision==null){
			if(sDate.year > eDate.year)
				return false;
			if(sDate.year < eDate.year)
				return true;
			if(sDate.month==null || eDate.month==null || sDate.month < eDate.month)			
				return true;
			if(sDate.month > eDate.month)			
				return false;
			if(sDate.day==null || eDate.day==null || sDate.day <= eDate.day)			
				return true;
			return false;
//		}else if(sDate.yearPrecision!=null || eDate.yearPrecision!=null){
		} else {
			int precisionAdjust=0;
			Integer sYear = sDate.year;
			Integer eYear = eDate.year;
			if(sDate.yearPrecision!=null) {
				switch(sDate.yearPrecision) {
				case DECADE:
					precisionAdjust=10;
					break;
				case CENTURY:
					precisionAdjust=100;
					break;
				case MILLENIUM:
					precisionAdjust=1000;
					break;
				}
				sYear=(sYear / precisionAdjust) * precisionAdjust;
			}
			if(eDate.yearPrecision!=null) {
				switch(eDate.yearPrecision) {
				case DECADE:
					precisionAdjust=10;
					break;
				case CENTURY:
					precisionAdjust=100;
					break;
				case MILLENIUM:
					precisionAdjust=1000;
					break;
				}
				eYear=(eYear / precisionAdjust) * precisionAdjust;
			}
			return sYear<=eYear;
		}
	}

	private static boolean validateInstant(Instant edtf) {
		Date date = edtf.getDate();
		if(date.isUnkown() || date.isUnspecified())
			return true;
		if(date.year==null)
			return false;
		if(date.yearPrecision==null) {
			if(date.month!=null) {
				if(date.month<1 || date.month>12)
					return false;
				if(date.day!=null) {
					if(date.day<1 || date.day>31)
						return false;
					if(date.day==31 && !isMonthOf31Days(date.month))
						return false;
					if(date.month==2)
						if(date.day==30 || (date.day==29 && !Year.isLeap(date.year)))
							return false;
				}
			}
		}
		Time t = edtf.getTime();
		if(t!=null) {
			if(t.hour>=24 || t.hour<0)
				return false;
			if(t.minute!=null && (t.minute>=60 || t.minute<0))
				return false;
			if(t.second!=null && (t.second>=60 || t.second<0))
				return false;
			if(t.millisecond!=null && (t.millisecond>=1000 || t.millisecond<0))
				return false;
			//TODO: validate timezone
		}
		return true;
	}

	private static boolean isMonthOf31Days(Integer month) {
		month-=1;//java Calendar starts at month 0
        return ((month == Calendar.JANUARY) || 
                (month == Calendar.MARCH) || 
                (month == Calendar.MAY) ||
                (month == Calendar.JULY) || 
                (month == Calendar.AUGUST) || 
                (month == Calendar.OCTOBER) ||
                (month == Calendar.DECEMBER));
	}

}
