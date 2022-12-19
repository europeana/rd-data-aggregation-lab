package europeana.rnd.dataprocessing.dates.publication;

import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.edtf.Instant;

public enum PatternId {
	NO_MATCH_0,
	INVALID_I, 
//	Edtf, 
//	Edtf_Range, 
	ISO8601, 
//	ISO8601_Range, 
	ISO8601_Cleaned,
//	ISO8601_Cleaned_Range,

//	MONTH_NAME_Range,

	Numeric_AllVariants_1, 
	Numeric_Spaces_2, 
	Numeric_AllVariants_Range_3,
	
	DCMIPeriod_8,
	FormatedFullDate_14,
	Century_Roman_6,
	Century_Roman_Range_7,
	MONTH_NAME_YYYY_5,
	MONTH_NAME_YYYY_DD_4,
	BcAd_9, 
	BcAd_Range_10, 
	Brief_Date_Range_11, 
	LongYear_12,
	LongYear_Range_13;

//	NO_MATCH_N,
//	INVALID_I, 
////	Edtf, 
////	Edtf_Range, 
//	ISO8601, 
//	ISO8601_Range, 
//	ISO8601_Cleaned,
//	ISO8601_Cleaned_Range,
//	DCMIPeriod_8,
//	FormatedFullDate_14,
//	Century_Roman_6,
//	Century_Roman_Range_7,
//	Century_Decade_Numeric,
//	Century_Decade_Numeric_Range,
//	Numeric_AllVariants, 
//	Numeric_AllVariants_Range, 
//	MONTH_NAME,
//	MONTH_NAME_Range,
//	BcAd_9, 
//	BcAd_Range_10, 
//	Brief_Date_Range_11, 
//	LongYear_12,
//	LongYear_Range_13;
	
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
		case NO_MATCH: return PatternId.NO_MATCH_0;
		case INVALID: return PatternId.NO_MATCH_0;
		case BcAd: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.BcAd_9 : PatternId.BcAd_Range_10);
		case Century_Numeric: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.Numeric_AllVariants_1 : PatternId.Numeric_AllVariants_Range_3);
		case Century_Range_Roman: return PatternId.Century_Roman_Range_7;
		case Century_Roman: return PatternId.Century_Roman_6;
		case DCMIPeriod: return PatternId.DCMIPeriod_8;
		case Decade: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.Numeric_AllVariants_1 : PatternId.Numeric_AllVariants_Range_3);
		case Edtf: return PatternId.ISO8601;
//		case Edtf: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.ISO8601 : PatternId.ISO8601_Range);
		case Edtf_Cleaned: return PatternId.ISO8601_Cleaned;
//		case Edtf_Cleaned: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.ISO8601_Cleaned : PatternId.ISO8601_Cleaned_Range);
		case FormatedFullDate: return PatternId.FormatedFullDate_14;
		case MONTH_NAME: { 
			if (m.getExtracted().getEdtf() instanceof Instant){
				if (((Instant)m.getExtracted().getEdtf()).getDate().getDay()==null)
					return PatternId.MONTH_NAME_YYYY_5;
				else
					return PatternId.MONTH_NAME_YYYY_DD_4;
			} else
				return PatternId.NO_MATCH_0;
		}
		case Numeric_AllVariants: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.Numeric_AllVariants_1 : PatternId.Numeric_AllVariants_Range_3);
		case Numeric_AllVariants_Xx: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.Numeric_AllVariants_1 : PatternId.Numeric_AllVariants_Range_3);
		case Numeric_Range_AllVariants: return PatternId.Numeric_AllVariants_Range_3;
		case Numeric_Range_AllVariants_Xx: return PatternId.Numeric_AllVariants_Range_3;
		case YYYY_MM_DD_Spaces: return PatternId.Numeric_Spaces_2;
		case Brief_Date_Range: return PatternId.Brief_Date_Range_11;
		case LongYear: return (m.getExtracted().getEdtf() instanceof Instant ? PatternId.LongYear_12 : PatternId.LongYear_Range_13);
		default:
			throw new IllegalArgumentException(m.getMatchId().getLabel().toString());
		}
	}
	
	public String getLabel() {
		switch (this) {
//		case BcAd_9:
//			return "normalisable: BC/AD date";
//		case BcAd_Range_10:
//			return "normalisable: BC/AD date (range)";
//		case Brief_Date_Range_11:
//			return "normalisable: brief year range";
//		case Century_Decade_Numeric:
//			return "normalisable: century or decade (numeric)";
//		case Century_Decade_Numeric_Range:
//			return "normalisable: century or decade (numeric) (range)";
//		case Century_Roman_6:
//			return "normalisable: century (roman numerals)";
//		case Century_Roman_Range_7:
//			return "normalisable: century (roman numerals) (range)";
//		case DCMIPeriod_8:
//			return "normalisable: DCMI period";
//		case ISO8601:
//			return "already normalised in EDTF or ISO8601";
//		case ISO8601_Range:
//			return "already normalised in EDTF or ISO8601 (range)";
//		case FormatedFullDate_14:
//			return "normalisable: formated timestamp";
//		case INVALID_I:
//			return "not normalisable: date apparently invalid";
//		case ISO8601_Cleaned:
//			return "normalisable: in EDTF or ISO8601 with data cleaning";
//		case ISO8601_Cleaned_Range:
//			return "normalisable: in EDTF or ISO8601 with data cleaning (range)";
//		case LongYear_12:
//			return "normalisable: long negative year";
//		case LongYear_Range_13:
//			return "normalisable: long negative year (range)";
//		case MONTH_NAME_YYYY_5:
//			return "normalisable: date with month name";
//		case MONTH_NAME_Range:
//			return "normalisable: date with month name (range)";
//		case NO_MATCH_0:
//			return "not normalisable: no match with existing patterns";
//		case Numeric_AllVariants_:
//			return "normalisable: numeric date (various separators)";
//		case Numeric_AllVariants_Range_3:
//			return "normalisable: numeric date (various separators) (range)";
		default:
			return this.name();
		}
	}
}
