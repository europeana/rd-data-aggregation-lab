package europeana.rnd.dataprocessing.dates.extraction;

public enum MatchId {
	NO_MATCH,
	YYYY, xYYYYx
	,YYYY_Range_Open, YYYY_Range, DCMIPeriod, YYYY_MM_DD, xYYYY_MM_DDx,
	MONTH_NAME_DAY_YEAR, MONTH_NAME_YEAR, FormatedFullDate, YYYY_MM, xYYYY_MMx, YYYY_CA, SquareBrackets, DD_MM_YYYY, xDD_MM_YYYYx, Century, CenturyX
}
