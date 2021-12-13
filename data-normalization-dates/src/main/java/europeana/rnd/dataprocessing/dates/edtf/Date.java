package europeana.rnd.dataprocessing.dates.edtf;

public class Date {
	public enum YearPrecision { MILLENIUM, CENTURY, DECADE, YEAR };
	
	public static final Date UNKNOWN=new Date() {
		{
			setUnkown(true);
		}
	};
	
	public static final Date UNSPECIFIED=new Date() {
		{
			setUnspecified(true);
		}
	};
	
	boolean uncertain;
	boolean approximate;
	boolean unkown;
	boolean unspecified;
	
	Integer year;	
	Integer month;	
	Integer day;
	
	YearPrecision yearPrecision;
	
	public boolean isUncertain() {
		return uncertain;
	}
	public void setUncertain(boolean uncertain) {
		this.uncertain = uncertain;
	}
	public boolean isApproximate() {
		return approximate;
	}
	public void setApproximate(boolean approximate) {
		this.approximate = approximate;
	}
	public boolean isUnkown() {
		return unkown;
	}
	public void setUnkown(boolean unkown) {
		this.unkown = unkown;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}
	public Integer getDay() {
		return day;
	}
	public void setDay(Integer day) {
		this.day = day;
	}
	public boolean isUnspecified() {
		return unspecified;
	}
	public void setUnspecified(boolean unspecified) {
		this.unspecified = unspecified;
	}
	public YearPrecision getYearPrecision() {
		return yearPrecision;
	}
	public void setYearPrecision(YearPrecision yearPrecision) {
		this.yearPrecision = yearPrecision;
	}

	
	
}
