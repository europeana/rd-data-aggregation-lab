package europeana.rnd.dataprocessing.dates;

import java.io.File;

import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.edtf.EdtfValidator;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import europeana.rnd.dataprocessing.dates.stats.DateExtractionStatistics;
import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

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


		DatesInRecord datesInRec=new DatesInRecord("http://data.europeana.eu/item/00000/exampleCHO", false, false);
		
		DateExtractionStatistics des=new DateExtractionStatistics();
		DateExtractionStatistics desSubCov=new DateExtractionStatistics();
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
//				"-2100/-1550"
//				"187-?]"
				
//				"18. September 1914",
//				"21.1.1921",
//				"2014/15",
//				"12.10.1690",
//				"26.4.1828",
//				"19151231",
//				"28.05.1969",
//				"28. 1. 1240",
//				"01?-1905",
//				"15/21-8-1918",
//				"199--09-28",
//				"19960216-19960619"
//				"-0549-01-01T00:00:00Z",
//				"Byzantine Period; start=0395; end=0641",
//				"1918-20",
//				"1942-1943 c.",
//	 			"[1942-1943]",
//	 			"(1942-1943)",
//	 			"(1942)",
//				"-3.6982",
//				"[ca. 16??]",
//				"[19--?]",
//				"ISO9126",
//				"SVV",
				"1985-10-xx",
		}) {
			datesInRec.addTo(Source.EUROPEANA, Ore.Proxy, Dc.date, Jena.createLiteral(test).asLiteral());
			System.out.println(test);
			r = DatesExtractorHandler.runDateNormalization(test);
			System.out.println(r.getCleanOperation());
			System.out.println(r.getMatchId());
			System.out.println(r.getExtracted());
			if (r.getMatchId()!=MatchId.NO_MATCH) {
				System.out.println("valid: "+EdtfValidator.validate(r.getExtracted(), false));
			}
		}
		DatesExtractorHandler.runDateNormalization(datesInRec);
		for(DateValue dv: datesInRec.getAllValuesDetailed(Source.ANY)) 
			des.add(datesInRec.getChoUri(), Source.EUROPEANA, dv);
//		des.save(new File("target"));	
		HtmlExporter.export(des, desSubCov, new File("target"));
	}
}
