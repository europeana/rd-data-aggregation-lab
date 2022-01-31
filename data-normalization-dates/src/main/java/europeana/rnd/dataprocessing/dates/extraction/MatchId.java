package europeana.rnd.dataprocessing.dates.extraction;

public enum MatchId {
	NO_MATCH,
	YYYY, xYYYYx
	,YYYY_Range_Open, YYYY_Range, DCMIPeriod, YYYY_MM_DD, xYYYY_MM_DDx
	,YYYYMMDD_Range_Open, YYYYMMDD_Range,
	MONTH_NAME_DAY_YEAR, MONTH_NAME_YEAR, FormatedFullDate, YYYY_MM, xYYYY_MMx, DD_MM_YYYY, xDD_MM_YYYYx, 
	Century_Numeric, 
	Century_Roman, Century_Range_Roman,
//	CenturyX, 
	Numeric_Range_AllVariants, Numeric_Range_AllVariants_Xx,
	Numeric_AllVariants, Decade, Numeric_AllVariants_Xx, 
	Iso8601Date, Iso8601BriefDate, Iso8601BriefDateRange, Iso8601DateRange, 
	YYYY_MM_DD_Spaces, BcAd, Edtf;

	public String getLabel() {
		switch (this) {
		case NO_MATCH: return "no match";
		case BcAd: return "BC/AD";
		case Century_Numeric: return "century (numeric)";
		case Century_Range_Roman: return "century range (roman numerals)";
		case Century_Roman: return "century (roman numerals)";
		case DCMIPeriod: return "DCMI period";
		case DD_MM_YYYY: return "dd--mm-yyyy";
		case Decade: return "decade";
		case Edtf: return "EDTF";
		case FormatedFullDate: return "formated timestamp";
		case Iso8601BriefDate: return "ISO 8601 brief";
		case Iso8601BriefDateRange: return "ISO 8601 brief (interval)";
		case Iso8601Date: return "ISO 8601";
		case Iso8601DateRange: return "ISO 8601 (interval)";
		case MONTH_NAME_DAY_YEAR: return "month name, day and year";
		case MONTH_NAME_YEAR: return "month name and year";
		case Numeric_AllVariants: return "numeric date (various separators)";
		case Numeric_AllVariants_Xx: return "numeric date (various separators and unknown parts)";
		case Numeric_Range_AllVariants: return "numeric date interval (various separators)";
		case Numeric_Range_AllVariants_Xx: return "numeric date interval (various separators and unknown parts)";
//		case YYYY: return "";
//		case YYYYMMDD_Range: return "";
//		case YYYYMMDD_Range_Open: return "";
//		case YYYY_MM: return "";
//		case YYYY_MM_DD: return "";
		case YYYY_MM_DD_Spaces: return "numeric date (whitespace separators)";
//		case YYYY_Range: return "";
//		case YYYY_Range_Open: return "";
//		case xDD_MM_YYYYx: return "";
//		case xYYYY_MM_DDx: return "";
//		case xYYYY_MMx: return "";
//		case xYYYYx: return "";
		default:
			return name();
		}
	}
}
