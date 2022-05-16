package europeana.rnd.dataprocessing.dates.publication;

import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.extraction.Match;

public enum PatternId {
	NO_MATCH,
	INVALID, 
//	Edtf, 
//	Edtf_Range, 
	ISO8601, 
	ISO8601_Range, 
	ISO8601_Cleaned,
	ISO8601_Cleaned_Range,
	DCMIPeriod,
	FormatedFullDate,
	Century_Roman,
	Century_Roman_Range,
	Century_Decade_Numeric,
	Century_Decade_Numeric_Range,
	Numeric_AllVariants, 
	Numeric_AllVariants_Range, 
	MONTH_NAME,
	MONTH_NAME_Range,
	BcAd, 
	BcAd_Range, 
	Brief_Date_Range, 
	LongYear,
	LongYear_Range;
//	
//	
//	,YYYY_Range_Open, YYYY_Range, 
//	YYYY_MM_DD, xYYYY_MM_DDx
//	,YYYYMMDD_Range_Open, YYYYMMDD_Range,
//	//MONTH_NAME_DAY_YEAR,
//	MONTH_NAME, YYYY_MM, xYYYY_MMx, DD_MM_YYYY, xDD_MM_YYYYx, 
//	 
//	 Century_Range_Roman,
////	CenturyX, 
//	Numeric_Range_AllVariants,
//	Decade, 
//	YYYY_MM_DD_Spaces, 

	public static PatternId fromMatch(Match m) {
		switch (m.getMatchId()) {
		case NO_MATCH: return PatternId.NO_MATCH;
		case INVALID: return PatternId.NO_MATCH;
		case BcAd: return (m.getExtracted() instanceof Instant ? PatternId.BcAd : PatternId.BcAd_Range);
		case Century_Numeric: return (m.getExtracted() instanceof Instant ? PatternId.Century_Decade_Numeric : PatternId.Century_Decade_Numeric_Range);
		case Century_Range_Roman: return PatternId.Century_Roman_Range;
		case Century_Roman: return PatternId.Century_Roman;
		case DCMIPeriod: return PatternId.DCMIPeriod;
		case Decade: return (m.getExtracted() instanceof Instant ? PatternId.Century_Decade_Numeric : PatternId.Century_Decade_Numeric_Range);
		case Edtf: return (m.getExtracted() instanceof Instant ? PatternId.ISO8601 : PatternId.ISO8601_Range);
		case Edtf_Cleaned: return (m.getExtracted() instanceof Instant ? PatternId.ISO8601_Cleaned : PatternId.ISO8601_Cleaned_Range);
		case FormatedFullDate: return PatternId.FormatedFullDate;
		case MONTH_NAME: return (m.getExtracted() instanceof Instant ? PatternId.MONTH_NAME : PatternId.MONTH_NAME_Range);
		case Numeric_AllVariants: return (m.getExtracted() instanceof Instant ? PatternId.Numeric_AllVariants : PatternId.Numeric_AllVariants_Range);
		case Numeric_AllVariants_Xx: return (m.getExtracted() instanceof Instant ? PatternId.Numeric_AllVariants : PatternId.Numeric_AllVariants_Range);
		case Numeric_Range_AllVariants: return PatternId.Numeric_AllVariants_Range;
		case Numeric_Range_AllVariants_Xx: return PatternId.Numeric_AllVariants_Range;
		case YYYY_MM_DD_Spaces: return (m.getExtracted() instanceof Instant ? PatternId.Numeric_AllVariants : PatternId.Numeric_AllVariants_Range);
		case Brief_Date_Range: return PatternId.Brief_Date_Range;
		case LongYear: return (m.getExtracted() instanceof Instant ? PatternId.LongYear : PatternId.LongYear_Range);
		default:
			throw new IllegalArgumentException(m.getMatchId().getLabel().toString());
		}
	}
	
	public String getLabel() {
		switch (this) {
		case BcAd:
			return "normalisable: BC/AD date";
		case BcAd_Range:
			return "normalisable: BC/AD date (range)";
		case Brief_Date_Range:
			return "normalisable: brief year range";
		case Century_Decade_Numeric:
			return "normalisable: century or decade (numeric)";
		case Century_Decade_Numeric_Range:
			return "normalisable: century or decade (numeric) (range)";
		case Century_Roman:
			return "normalisable: century (roman numerals)";
		case Century_Roman_Range:
			return "normalisable: century (roman numerals) (range)";
		case DCMIPeriod:
			return "normalisable: DCMI period";
		case ISO8601:
			return "already normalised in EDTF or ISO8601";
		case ISO8601_Range:
			return "already normalised in EDTF or ISO8601 (range)";
		case FormatedFullDate:
			return "normalisable: formated timestamp";
		case INVALID:
			return "not normalisable: date apparently invalid";
		case ISO8601_Cleaned:
			return "normalisable: in EDTF or ISO8601 with data cleaning";
		case ISO8601_Cleaned_Range:
			return "normalisable: in EDTF or ISO8601 with data cleaning (range)";
		case LongYear:
			return "normalisable: long negative year";
		case LongYear_Range:
			return "normalisable: long negative year (range)";
		case MONTH_NAME:
			return "normalisable: date with month name";
		case MONTH_NAME_Range:
			return "normalisable: date with month name (range)";
		case NO_MATCH:
			return "not normalisable: no match with existing patterns";
		case Numeric_AllVariants:
			return "normalisable: numeric date (various separators)";
		case Numeric_AllVariants_Range:
			return "normalisable: numeric date (various separators) (range)";
		default:
			return this.name();
		}
	}
}
