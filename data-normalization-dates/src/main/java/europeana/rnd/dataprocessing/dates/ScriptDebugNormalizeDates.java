package europeana.rnd.dataprocessing.dates;

import europeana.rnd.dataprocessing.dates.extraction.Match;

public class ScriptDebugNormalizeDates {
	public static void main(String[] args) throws Exception {
//	      String  str= "400 BC - 400 AD";
//	      System.out.println(str);
//	      for (char c : str.toCharArray()) {
//	         System.out.printf("\\u%04x \n", (int) c); 
//	      }
//	      str = "235 AD – 236 AD";
//	      System.out.println(str);
//	      for (char c : str.toCharArray()) {
//	    	  System.out.printf("\\u%04x \n", (int) c); 
//	      }

		
		
		Match r;
		for(String test: new String[] {
//				"XIV",
//				"1905 09 01",
//				"1851-01-01  - 1851-12-31",
//				"1952-02-25T00:00:00Z-1952-02-25T23:59:59Z",, has wrong period separator
//				" 1820/1820", //strange 1st character 
//				"1910/05/31 | 1910/05/01",
//				"1650? - 1700?",
//				"1916-09-26 1916-09-28", //missing period separator
//				"1937--1938",unsuported separator
//				"18..",
//				"2013-09-07 09:31:51 UTC",
//				"1918 / 1919",

//				"1205/1215 [Herstellung]",
//				"1997-07-18T00:00:00 [Create]",
//				"1924 ca.",
//				" 1757/1757",
//				"ca 1757/1757",
				
//				"2000 vC - 2002 nC",
//				"0114 aC - 0113 aC",
//				"0390 AD - 0425 AD",
//				"337 BC - 283 BC",
//				"100 vC - 150 nC",
//				"400 BC - 400 AD",
//				"235 AD – 236 AD",
//				"S. XVI-XX",

				
//				"19??",
//				"192?",
//				"[1712?]",	
//				"circa 1712",	
//				"[171-]",	
//				"[ca. 1946]",
//				"[ca. 193-]",

//				"1651?]",
//				"19--?]",
//				". 1885",
//				"- 1885",
//				"192?-1958",
//
//				"1749 (Herstellung (Werk))",
				

//				"1939; 1954; 1955; 1978; 1939-1945",

				
				
//				"[ca. 1920-1930]",


				
//				"1911]"
//				"[17__]"
//				"19--]"
//				"1939 [1942?]",
//				"S.VIII-XV"
//				"S. XVIII-"
//				"S. XVI-XVIII"
//				"[XVI-XIX]"
//				"1972/10/31 | 1972/10/01"
//				"19xx"
//				"Sat Jan 01 01:00:00 CET 1701",
//				"2013-03-21 18:45:36 UTC"

//				"15.02.1985 (identification)"

				
//				"-500000",
//				"091090",
//				"-0043-12-07",				
//				"1651 [ca. 1656]",
//				"imp. 1901"
				
//"-701950/-251950",
//"18720601/18720630","1872-06-01/1872-06-30"
//"19471950/19501953","1947-19-50/1950-19-53"
//				"169-? - 170-?"				
//				"u.1707-1739"
				

//				"29-10-2009 29-10-2009"
//				"MDCLXX"
//				"MDCVII"
//				"10th century"
//				"12th century BC"
//				"1952-02-25T00:00:00Z-1952-02-25T23:59:59Z" wrong range separator - do not support

				
//				"1990-"
//				"22.07.1971 (identification)"
				"-2100/-1550"
		}) {
			System.out.println(test);
			r = DatesExtractorHandler.runDateNormalization(test);
			System.out.println(r.getCleanOperation());
			System.out.println(r.getMatchId());
			System.out.println(r.getExtracted());
		}
		

	}
}
