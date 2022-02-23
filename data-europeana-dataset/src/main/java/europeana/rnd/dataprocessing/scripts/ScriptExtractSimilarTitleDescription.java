package europeana.rnd.dataprocessing.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
import inescid.util.europeana.EdmRdfUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ScriptExtractSimilarTitleDescription extends EuropeanaDatasetProcessorHandler {
	boolean testing=false;
	int casesFound=0;
	File outCsvFile; 
	CSVPrinter csvPrinter;
	FileWriterWithEncoding csvFileWriter;

	public static void main(String[] args) throws Exception {
//		EuropeanaDatasetProcessor.run(args, new ScriptExtractSimilarTitleDescription());
		
		filterCsv(new File("C:\\Users\\nfrei\\Desktop\\data\\problem_pattern_3\\problem_pattern_3.csv"));
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
		Resource proxy = EdmRdfUtil.getProxyOfProvider(edm);
		
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
	
	private static void filterCsv(File inCsvFile) throws IOException {
		Reader reader=new FileReader(inCsvFile, StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		
		File outCsvFile98=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.98-1.00.csv");
		File outCsvFile95=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.95-0.98.csv");
		File outCsvFile90=new File(inCsvFile.getParentFile(), "problem_pattern_3_0.90-0.95.csv");
		
		Writer csvFileWriter98=new FileWriterWithEncoding(outCsvFile98, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter98=new CSVPrinter(csvFileWriter98, CSVFormat.DEFAULT);
		Writer csvFileWriter95=new FileWriterWithEncoding(outCsvFile95, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter95=new CSVPrinter(csvFileWriter95, CSVFormat.DEFAULT);
		Writer csvFileWriter90=new FileWriterWithEncoding(outCsvFile90, StandardCharsets.UTF_8);
		CSVPrinter csvPrinter90=new CSVPrinter(csvFileWriter90, CSVFormat.DEFAULT);

		boolean first=true;
		for(CSVRecord rec: parser) {
			if(first) {
				first=false;
				for(String v: rec) {
					csvPrinter98.print(v);
					csvPrinter95.print(v);
					csvPrinter90.print(v);
				}
				csvPrinter98.println();
				csvPrinter95.println();
				csvPrinter90.println();
				continue;
			}
			double opt3=Double.parseDouble(rec.get(7));
			if (opt3 > 0.98) {
				for(String v: rec)
					csvPrinter98.print(v);
				csvPrinter98.println();
			} else if (opt3 > 0.95) {
				for(String v: rec)
					csvPrinter95.print(v);
				csvPrinter95.println();				
			} else if (opt3 > 0.9) {
				for(String v: rec)
					csvPrinter90.print(v);
				csvPrinter90.println();								
			}
		}
		parser.close();
		reader.close();
		csvPrinter98.close();
		csvFileWriter98.close();
		csvPrinter95.close();
		csvFileWriter95.close();
		csvPrinter90.close();
		csvFileWriter90.close();
	}
}
