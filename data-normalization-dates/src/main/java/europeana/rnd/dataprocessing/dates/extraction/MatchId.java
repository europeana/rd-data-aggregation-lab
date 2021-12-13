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
	YYYY_MM_DD_Spaces, BcAd
}
