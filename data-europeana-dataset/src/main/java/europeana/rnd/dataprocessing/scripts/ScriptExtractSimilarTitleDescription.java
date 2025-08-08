package europeana.rnd.dataprocessing.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import europeana.rnd.dataprocessing.EuropeanaDatasetProcessor;
import europeana.rnd.dataprocessing.EuropeanaDatasetProcessorHandler;
import inescid.dataaggregation.data.model.CreativeCommons;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.europeana.EdmRdfUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;

public class ScriptExtractSimilarTitleDescription extends EuropeanaDatasetProcessorHandler {
	boolean testing=false;
	int casesFound=0;
	File outCsvFile; 
	CSVPrinter csvPrinter;
	FileWriterWithEncoding csvFileWriter;

	public static void main(String[] args) throws Exception {
//		EuropeanaDatasetProcessor.run(args, new ScriptExtractSimilarTitleDescription());
		
//		filterCsv(new File("C:\\Users\\nfrei\\Desktop\\data\\problem_pattern_3\\problem_pattern_3.csv"));
//		filterCsvOpt3_4(new File("C:\\Users\\nfrei\\Desktop\\data\\problem_pattern_3\\problem_pattern_3.csv"));
		File dataFolder=new File("C:\\Users\\nfrei\\Desktop\\data\\problem_pattern_3");
		
//		reviseFormulas(new File(dataFolder, "problem_pattern_3.csv"));
//		filterFile(new File(dataFolder ,"problem_pattern_3_new_formulas.csv"), new File(dataFolder ,"problem_pattern-f3-0_8-0_9.csv"), 0.8, 0.9, true);
//		filterFile(new File(dataFolder ,"problem_pattern_3_new_formulas.csv"), new File(dataFolder ,"problem_pattern-f3-0_7-0_8.csv"), 0.7, 0.8, true);

//		reviseFormulasWithLcs(new File(dataFolder, "problem_pattern_3.csv"));
//		filterFileLcs(new File(dataFolder ,"problem_pattern_3_lcs.csv"), new File(dataFolder ,"problem_pattern_3_lcs_filtered_30.csv"), 0, 30, 1000, true);
//		filterFileLcs(new File(dataFolder ,"problem_pattern_3_lcs.csv"), new File(dataFolder ,"problem_pattern_3_lcs_filtered_50.csv"), 31, 50, 1000, true);
//		filterFileLcs(new File(dataFolder ,"problem_pattern_3_lcs.csv"), new File(dataFolder ,"problem_pattern_3_lcs_filtered_100.csv"), 51, 100, 1000, true);

		
		reviseFormulasWithLcsLevDisagree(new File(dataFolder, "problem_pattern_3.csv"));
		
		printStatsOfScore3AndLcs(new File(dataFolder, "problem_pattern_3.csv"));
		
		
		
		//a test
//		{
//			String t="SOPHIA LOREN PLAYS AN ILLEGAL IMMIGRANT";
//			String d="SOPHIA LOREN PLAYS AN ILLEGAL IMMIGRANT IN ISRAEL - 24 NOVEMBER 1964";
//			LevenshteinDistance lev=new LevenshteinDistance();
//			double dist = lev.apply(t, d);
//			
//			double lenTitle=t.length();
//			double lenDesc=d.length();
//			
//			double lenMax= Math.max(lenDesc, lenTitle);
//			double lenMin= Math.max(lenDesc, lenTitle);
//	
//			double opt3= 1d - (dist / lenMax); 
//			double opt4= 1 - dist / Math.pow(lenMax, 1.2); 
//	
//			double opt5=new JaroWinklerSimilarity().apply(t, d);
//			
//			System.out.println(opt3);
//			System.out.println(opt4);
//			System.out.println("J-W:"+opt5);
//			System.out.println("len t:"+lenTitle);
//			System.out.println("len d:"+lenDesc);
//			System.out.println("len d-t:"+(lenDesc-lenTitle));
//			
//			System.out.println(new JaroWinklerSimilarity().apply("mit Anschreiben", "Anschreiben"));
//			System.out.println(new JaroWinklerSimilarity().apply("Anschreiben mit", "Anschreiben"));
//			System.out.println(new JaroWinklerSimilarity().apply("Anschreiben", "Anschreiben mit"));
//			System.out.println(new LongestCommonSubsequence().apply("mit Anschreiben", "Anschreiben"));
//			System.out.println(new LongestCommonSubsequence().apply("Anschreiben mit", "Anschreiben"));
//			System.out.println(new LongestCommonSubsequence().apply("Anschreiben", "Anschreiben mit"));
//		}		
		
		ArrayList<String[]> tests=new ArrayList<String[]>();
		tests.add(new String[] {"mit Anschreiben", "Anschreiben"});
		tests.add(new String[] {"SOPHIA LOREN PLAYS AN ILLEGAL IMMIGRANT", "SOPHIA LOREN PLAYS AN ILLEGAL IMMIGRANT IN ISRAEL - 24 NOVEMBER 1964"});
		tests.add(new String[] {"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				                "1234567890123 567890123456789012345678901234567890123456 8901234567890123456789 012345678901234567890abcdefghijabcdefghijabcdefghij"});
		
		for(String[] tst: tests) {
			System.out.println(tst[0]);
			System.out.println(tst[1]);
			double lcs = new LongestCommonSubsequence().apply(tst[0], tst[1]);
			System.out.println(lcs);
			double lvs = new LevenshteinDistance().apply(tst[0], tst[1]);
			System.out.println(lvs);
			
			double lenTitle=tst[0].length();
			double lenDesc=tst[1].length();
			
			double minLen=Math.min(lenTitle, lenDesc);
			double difLen=Math.abs(lenTitle - lenDesc);
			
			System.out.println(lcs - minLen);
			System.out.println(lvs - minLen);
			System.out.println(lcs / minLen);
			System.out.println(lvs / minLen);
			
			
		}
		
		
		
	}
	
	private static void filterFile(File inCsvFile, File outCsvFile, double low, double high, boolean splitLines)  throws IOException {
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);

		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				for(String v: rec) {
					if(first) 
						first=false;
					else
						csvPrinter98.print(v);
				}
				csvPrinter98.println();
				first=false;
				continue;
			}
			double opt3=Double.parseDouble(rec.get(6));
			if (opt3 > low && opt3 <= high) {
				boolean firstField=true;
				for(String v: rec) {
					if(splitLines && firstField) {
						firstField=false;
						csvPrinter98.print(v);							
						csvPrinter98.println();							
					} else
						csvPrinter98.print(v);
				}
				csvPrinter98.println();
			}
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
	}

	private static void filterFileLcs(File inCsvFile, File outCsvFile, int minLenDif, int maxLenDif, int maxResults, boolean splitLines)  throws IOException {
		HashSet<String> dupDetect=new HashSet<String>(maxResults);
		
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		csvPrinter98.printRecord("Title/Description", "Lev. dist.", "Score 3", "Sho. Com. Seq.", "Score LCS", "Len. Dif.", "Problem?");
		csvPrinter98.printRecord("", "", "1 - lev_dist / max(length_title, length_desc)", "", "lcs/min(length_title, length_desc)", "", "soce_lcs>0.9 and len_dif<30");

		boolean first=true;
		int cnt=0;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				continue;
			}
			if(rec.size()>=5) {
				String title=rec.get(0);
				if(dupDetect.contains(title))
					continue;
				String lcsMatch=rec.get(7);
				double scoreLcs=Double.parseDouble(rec.get(5));
				int lenDif=(int) Double.parseDouble(rec.get(6));
//				if (lcsMatch.equals("Y")) {
				if ( scoreLcs > 0.9 
						&& lenDif>=minLenDif && lenDif<maxLenDif) {
					dupDetect.add(title);
					cnt++;
					boolean firstField=true;
					for(String v: rec) {
						if(splitLines && firstField) {
							firstField=false;
							csvPrinter98.print(v);							
							csvPrinter98.println();							
						} else
							csvPrinter98.print(v);
					}
					csvPrinter98.println();
				}
			}
			if(cnt>=maxResults)
				break;
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
	}
	
	@Override
	public void initParameters(String[] args) {
		super.initParameters(args);
		try {
			if (args != null && args.length >= 1) {
				outCsvFile = new File(args[1]);
			}else {
				outCsvFile = new File("c://users/nfrei/desktop/data/problem_pattern_3/problem_pattern_3.csv");
				testing=true;
			}
			csvFileWriter=new FileWriterWithEncoding(outCsvFile, StandardCharsets.UTF_8);
			csvPrinter=new CSVPrinter(csvFileWriter, CSVFormat.DEFAULT);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void initProcessing() {
		super.initProcessing();
		try {
			csvPrinter.printRecord("Title", "Description", "Lev. dist.", "Title length", "Description length", "Score 1", "Score 2", "Score 3", "Score 4");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public void closeProcessing() {
		super.closeProcessing();
		try {
			csvPrinter.close();
			csvFileWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public boolean handleRecord(Model edm, int currentRecordIndex) {
		Resource proxy = EdmRdfUtil.getProxyOfDataProvider(edm);
		
		StmtIterator titles = proxy.listProperties(Dc.title);
		StmtIterator descriptions = proxy.listProperties(Dc.description);
		
		REC: for(Statement titleSt: titles.toList()) {
			String titleStr = RdfUtil.getUriOrLiteralValue(titleSt.getObject());
			for(Statement descSt: descriptions.toList()) {
				String descStr = RdfUtil.getUriOrLiteralValue(descSt.getObject());
				if(descStr.length() > titleStr.length()) {
					LevenshteinDistance lev=new LevenshteinDistance();
					double dist = lev.apply(titleStr, descStr);
					
					double lenTitle=titleStr.length();
					double lenDesc=descStr.length();
					
					double lenMax= Math.max(lenDesc, lenTitle);
					double lenMin= Math.max(lenDesc, lenTitle);

					double opt3= 1d - (dist / lenMax); 
					if(opt3>0.5) {
						double opt1=dist - lenMin; //test < 0
						double opt2=dist / lenMax; //test > 0.5
						double opt4=1 - dist / Math.pow(lenMax, 1.2); 
						try {
							csvPrinter.printRecord(titleStr, descStr, 
									dist, titleStr.length(), descStr.length(),
									opt1, opt2, opt3, opt4);
						} catch (IOException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					
//					Option 1:
//						levenshtein_distance(title, description)<min(length(title), length(description))
//
//						levenshtein_distance(title, description) / min(length(title), length(description)) > p
//
//						Option 2: levenshtein_distance(title, description) > 0.5 * max(length(title), length(description))
//
//						Option 3: 1 - levenshtein_distance(title, description) / max(length(title), length(description)) > threshold (e.g. 0.5)
//
//						Option 4: 1 - levenshtein_distance(title, description) / max(length(title), length(description))^1.2 > threshold (e.g. 0.5)
				}
			}
		}
		
		return !testing || casesFound<10;
	}
	
	
	private static void filterCsvOpt3_4(File inCsvFile) throws IOException {
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_4.csv");
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		
		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				for(String v: rec) {
					csvPrinter98.print(v);
				}
				csvPrinter98.println();
				continue;
			}
			double opt3=Double.parseDouble(rec.get(7));
			double opt4=Double.parseDouble(rec.get(8));
			if (opt3 < 0.8 && opt4>0.8) {
				for(String v: rec)
					csvPrinter98.print(v);
				csvPrinter98.println();
			}
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
	}
	
	private static void reviseFormulas(File inCsvFile) throws IOException {
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_new_formulas.csv");
//		File outCsvFileTwoLines=new File(inCsvFile.getParentFile(), "problem_pattern_3_new_formulas_2lines.csv");
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		
//		Writer csvFileWriterTwoLines=new FileWriterWithEncoding(outCsvFileTwoLines, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinterTwoLines=new CSVPrinter(csvFileWriterTwoLines, CSVFormat.DEFAULT);
		
		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				for(String v: rec) {
					csvPrinter98.print(v);
				}
				csvPrinter98.println();
				continue;
			}
			
			String title=rec.get(0);
			String desc=rec.get(1);
			double dist = Double.parseDouble(rec.get(2));
			double lenTitle=Double.parseDouble(rec.get(3));
			double lenDesc=Double.parseDouble(rec.get(4));		

			double lenMax= Math.max(lenDesc, lenTitle);
			double lenMin= Math.max(lenDesc, lenTitle);

			double opt1=dist / Math.abs(lenTitle-lenDesc);
			
			double opt4=1 - dist / Math.pow(lenMax, 1.1); 
			
			double opt3=Double.parseDouble(rec.get(7));

			double opt5=new JaroWinklerSimilarity().apply(title, desc); 
			
//			boolean nunoFormula=false;
//			if(opt5>)
			
			
			csvPrinter98.printRecord(title, desc, 
					dist, lenTitle, lenDesc,
					opt1, opt3, opt4, opt5);
//			csvPrinterTwoLines.printRecord(title);
//			csvPrinterTwoLines.printRecord(desc, 
//					dist, lenTitle, lenDesc,
//					opt1, opt3, opt4, opt5);
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
//		csvPrinterTwoLines.close();
//		csvFileWriterTwoLines.close();
	}

	private static void reviseFormulasWithLcs(File inCsvFile) throws IOException {
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_lcs.csv");
//		File outCsvFileTwoLines=new File(inCsvFile.getParentFile(), "problem_pattern_3_new_formulas_2lines.csv");
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		
//		Writer csvFileWriterTwoLines=new FileWriterWithEncoding(outCsvFileTwoLines, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinterTwoLines=new CSVPrinter(csvFileWriterTwoLines, CSVFormat.DEFAULT);
		
		csvPrinter98.printRecord("Title/Description", "Lev. dist.", "Score 3", "Sho. Com. Seq.", "Len. Dif.", "Problem?");
//		csvPrinter98.printRecord("", "", "1 - lev_dist / max(length_title, length_desc)", "", "", "scv>0.9 and len_dif<30");

		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				continue;
			}
			
			String title=rec.get(0);
			String desc=rec.get(1);
			double dist = Double.parseDouble(rec.get(2));
			double lenTitle=Double.parseDouble(rec.get(3));
			double lenDesc=Double.parseDouble(rec.get(4));		
			
			double lenMax= Math.max(lenDesc, lenTitle);
			double lenMin= Math.min(lenDesc, lenTitle);
			double lenDif= Math.abs(lenDesc - lenTitle);
				
			double opt3=Double.parseDouble(rec.get(7));
			
			double lcv=new LongestCommonSubsequence().apply(title, desc); 
			double opt5=lcv / lenMin;
			boolean opt5Match=opt5>0.9 && lenDif<30;

			csvPrinter98.printRecord(title, desc, dist, opt3, lcv, opt5, lenDif, opt5Match ? "Y" : "N");
//			csvPrinterTwoLines.printRecord(title);
//			csvPrinterTwoLines.printRecord(desc, 
//					dist, lenTitle, lenDesc,
//					opt1, opt3, opt4, opt5);
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
//		csvPrinterTwoLines.close();
//		csvFileWriterTwoLines.close();
	}
	
	
	private static void reviseFormulasWithLcsLevDisagree(File inCsvFile) throws IOException {
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_lcs_score3_disagree.csv");
//		File outCsvFileTwoLines=new File(inCsvFile.getParentFile(), "problem_pattern_3_new_formulas_2lines.csv");
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		
//		Writer csvFileWriterTwoLines=new FileWriterWithEncoding(outCsvFileTwoLines, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinterTwoLines=new CSVPrinter(csvFileWriterTwoLines, CSVFormat.DEFAULT);
		
		csvPrinter98.printRecord("Title/Description", "Lev. dist.", "Score 3", "Length distance", "Sho. Com. Seq.", "Len. Dif.", "Problem?");
//		csvPrinter98.printRecord("", "", "1 - lev_dist / max(length_title, length_desc)", "", "", "scv>0.9 and len_dif<30");
		
		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				continue;
			}
			
			String title=rec.get(0);
			String desc=rec.get(1);
			double dist = Double.parseDouble(rec.get(2));
			double lenTitle=Double.parseDouble(rec.get(3));
			double lenDesc=Double.parseDouble(rec.get(4));		
			
			double lenMax= Math.max(lenDesc, lenTitle);
			double lenMin= Math.min(lenDesc, lenTitle);
			double lenDif= Math.abs(lenDesc - lenTitle);
			
			double opt3=Double.parseDouble(rec.get(7));
			
			double lcv=new LongestCommonSubsequence().apply(title, desc); 
			double opt5=lcv / lenMin;
			boolean opt5Match=opt5>0.9 && lenDif<30;

//			if (opt5<0.9 && opt3>0.9) -> 0 results
			if (lenDif>50 && opt3>0.9)
				csvPrinter98.printRecord(title, desc, dist, opt3, lcv, opt5, lenDif, opt5Match ? "Y" : "N");
//			csvPrinterTwoLines.printRecord(title);
//			csvPrinterTwoLines.printRecord(desc, 
//					dist, lenTitle, lenDesc,
//					opt1, opt3, opt4, opt5);
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
//		csvPrinterTwoLines.close();
//		csvFileWriterTwoLines.close();
	}
	
	
	
	private static void printStatsOfScore3AndLcs(File inCsvFile) throws IOException {
		MapOfInts<Integer> statsLcs9=new MapOfInts<Integer>();
		MapOfInts<Integer> statsLcs8=new MapOfInts<Integer>();
		MapOfInts<Double> statsLeven=new MapOfInts<Double>();
		
		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);

		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				continue;
			}
			
			String title=rec.get(0);
			String desc=rec.get(1);
			double dist = Double.parseDouble(rec.get(2));
			double lenTitle=Double.parseDouble(rec.get(3));
			double lenDesc=Double.parseDouble(rec.get(4));		
			
			double lenMax= Math.max(lenDesc, lenTitle);
			double lenMin= Math.min(lenDesc, lenTitle);
			double lenDif= Math.abs(lenDesc - lenTitle);
				
			double opt3=Double.parseDouble(rec.get(7));
			
			double lcv=new LongestCommonSubsequence().apply(title, desc); 
			double opt5=lcv / lenMin;
//			boolean opt5Match=opt5>0.9 && lenDif<30;

			if(opt5>=0.9) {
				if (lenDif<=30)
					statsLcs9.incrementTo(30);
				if (lenDif<=50)
					statsLcs9.incrementTo(50);
				if (lenDif<=80)
					statsLcs9.incrementTo(80);
				if (lenDif<=100)
					statsLcs9.incrementTo(100);
			}
			if(opt5>=0.85) {
				if (lenDif<=30)
					statsLcs8.incrementTo(30);
				if (lenDif<=50)
					statsLcs8.incrementTo(50);
				if (lenDif<=80)
					statsLcs8.incrementTo(80);
				if (lenDif<=100)
					statsLcs8.incrementTo(100);
			}
			
			
			if(opt3>0.9) 
				statsLeven.incrementTo(0.9);
			if(opt3>0.8) 
				statsLeven.incrementTo(0.8);
			 if(opt3>0.75) 
				statsLeven.incrementTo(0.75);
			 if(opt3>0.7) 
				statsLeven.incrementTo(0.7);
			 if(opt3>0.5) 
				statsLeven.incrementTo(0.5);
		}
		parser.close();
		reader.close();

		File outCsvStatsFile=new File(inCsvFile.getParentFile(), "problem_pattern_3_stats_lcs_levenshtein.csv");
		Writer csvFileWriter=new FileWriterWithEncoding(outCsvStatsFile, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter=new CSVPrinter(csvFileWriter, CSVFormat.DEFAULT);
		
		csvPrinter.printRecord("LCS","Len. Dif.","# Records");
		csvPrinter.printRecord(">0.9","<=30",statsLcs9.get(30));
		csvPrinter.printRecord(">0.9","<=50",statsLcs9.get(50));
		csvPrinter.printRecord(">0.9","<=80",statsLcs9.get(80));
		csvPrinter.printRecord(">0.9","<=100",statsLcs9.get(100));
		csvPrinter.printRecord(">0.85","<=30",statsLcs8.get(30));
		csvPrinter.printRecord(">0.85","<=50",statsLcs8.get(50));
		csvPrinter.printRecord(">0.85","<=80",statsLcs8.get(80));
		csvPrinter.printRecord(">0.85","<=100",statsLcs8.get(100));
		csvPrinter.println();
		csvPrinter.printRecord("Score 3","# Records");
		csvPrinter.printRecord(">0.9",statsLeven.get(0.9));
		csvPrinter.printRecord(">0.8",statsLeven.get(0.8));
		csvPrinter.printRecord(">0.75",statsLeven.get(0.75));
		csvPrinter.printRecord(">0.7",statsLeven.get(0.7));
		csvPrinter.printRecord(">0.5",statsLeven.get(0.5));
		
		csvPrinter.close();
		csvFileWriter.close();
	}
	
	
	
}


//private static void filterCsv(File inCsvFile) throws IOException {
//		Reader reader=new InputStreamReader(new FileInputStream(inCsvFile), StandardCharsets.UTF_8);
//		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
//		
//		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.98-1.00.csv");
//		File outCsvFile95=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.95-0.98.csv");
//		File outCsvFile90=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.90-0.95.csv");
//		File outCsvFile80=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.80-0.90.csv");
//		
//		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
//		Writer csvFileWriter95=new FileWriterWithEncoding(outCsvFile95, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinter95=new CSVPrinter(csvFileWriter95, CSVFormat.DEFAULT);
//		Writer csvFileWriter90=new FileWriterWithEncoding(outCsvFile90, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinter90=new CSVPrinter(csvFileWriter90, CSVFormat.DEFAULT);
//		Writer csvFileWriter80=new FileWriterWithEncoding(outCsvFile80, StandardCharsets.UTF_8);
//		CSVPrinter csvPrinter80=new CSVPrinter(csvFileWriter80, CSVFormat.DEFAULT);
//
//		boolean first=true;
//		for(CSVRecord rec: parser) {
//			if(first) {
//				first=false;
//				for(String v: rec) {
//					csvPrinter98.print(v);
//					csvPrinter95.print(v);
//					csvPrinter90.print(v);
//					csvPrinter80.print(v);
//				}
//				csvPrinter98.println();
//				csvPrinter95.println();
//				csvPrinter90.println();
//				csvPrinter80.println();
//				continue;
//			}
//			double opt3=Double.parseDouble(rec.get(7));
//			if (opt3 > 0.98) {
//				for(String v: rec)
//					csvPrinter98.print(v);
//				csvPrinter98.println();
//			} else if (opt3 > 0.95) {
//				for(String v: rec)
//					csvPrinter95.print(v);
//				csvPrinter95.println();				
//			} else if (opt3 > 0.9) {
//				for(String v: rec)
//					csvPrinter90.print(v);
//				csvPrinter90.println();								
//			} else if (opt3 > 0.8) {
//				for(String v: rec)
//					csvPrinter80.print(v);
//				csvPrinter80.println();								
//			}
//		}
//		parser.close();
//		reader.close();
//		csvPrinter98.close();
//		csvFileWriter98.close();
//		csvPrinter95.close();
//		csvFileWriter95.close();
//		csvPrinter90.close();
//		csvFileWriter90.close();
//		csvPrinter80.close();
//		csvFileWriter80.close();
//	}
