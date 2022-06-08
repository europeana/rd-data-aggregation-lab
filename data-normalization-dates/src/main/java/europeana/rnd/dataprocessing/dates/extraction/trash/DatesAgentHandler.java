package europeana.rnd.dataprocessing.dates.extraction.trash;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.MatchId;
import europeana.rnd.dataprocessing.dates.Source;
import europeana.rnd.dataprocessing.dates.edtf.EdtfValidator;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;
import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.RdaGr2;

public class DatesAgentHandler {
	
	class DatesAgentStats {
		int beginOnly=0;
		int endOnly=0;
		int beginEnd=0;
		int birthOnly=0;
		int deathOnly=0;
		int birthDeath=0;
	}
	
	DatesAgentStats statsProvider=new DatesAgentStats();
	DatesAgentStats statsEuropeana=new DatesAgentStats();
	
	File outputFolder;

	public DatesAgentHandler(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void handle(DatesInRecord rec) throws Exception {
		handleFor(rec, Source.EUROPEANA, statsEuropeana);
		handleFor(rec, Source.PROVIDER, statsProvider);
	}
	
	private static void handleFor(DatesInRecord rec, Source src, DatesAgentStats stats) {
		List<Match> valuesForBegin = rec.getValuesFor(src, Edm.Agent, Edm.begin);
		List<Match> valuesForEnd = rec.getValuesFor(src, Edm.Agent, Edm.end);
		List<Match> valuesForBirth = rec.getValuesFor(src, Edm.Agent, RdaGr2.dateOfBirth);
		List<Match> valuesForDeath = rec.getValuesFor(src, Edm.Agent, RdaGr2.dateOfDeath);
		
		if(!valuesForBegin.isEmpty() && !valuesForEnd.isEmpty()) 
			stats.beginEnd++;
		else if(!valuesForBegin.isEmpty()) 
			stats.beginOnly++;
		else if(!valuesForEnd.isEmpty()) 
			stats.endOnly++;
		
		if(!valuesForBirth.isEmpty() && !valuesForDeath.isEmpty()) 
			stats.birthDeath++;
		else if(!valuesForBirth.isEmpty()) 
			stats.birthOnly++;
		else if(!valuesForDeath.isEmpty()) 
			stats.deathOnly++;		
	}
	
	public void close() {
		try {
			FileWriterWithEncoding writer=new FileWriterWithEncoding(new File(outputFolder, "agent_date_properties-provider.csv"), StandardCharsets.UTF_8);
			CSVPrinter printer=new CSVPrinter(writer, CSVFormat.DEFAULT);
			printer.printRecord("Source", "edm:begin only", "edm:end only", "both edm:begin and edm:end", "rdaGrp2:dateOfBirth only", "rdaGrp2:dateOfDeath only", "both rdaGrp2:dateOfBirth and rdaGrp2:dateOfDeath");
			printer.printRecord("Provider", statsProvider.beginOnly, statsProvider.endOnly, statsProvider.beginEnd, statsProvider.birthOnly, statsProvider.deathOnly, statsProvider.birthDeath);
			printer.printRecord("Europeana", statsEuropeana.beginOnly, statsEuropeana.endOnly, statsEuropeana.beginEnd, statsEuropeana.birthOnly, statsEuropeana.deathOnly, statsEuropeana.birthDeath);
			printer.close();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
}
