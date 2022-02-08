package europeana.rnd.dataprocessing.scripts;

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
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ScriptExtractSimilarTitleDescription extends EuropeanaDatasetProcessorHandler {
	int countLicenses=0;

	public static void main(String[] args) throws Exception {
		EuropeanaDatasetProcessor.run(args, new ScriptExtractSimilarTitleDescription());
	}

	@Override
	public boolean handleRecord(Model edm, int currentRecordIndex) {
		Resource proxy = EdmRdfUtil.getProxyOfProvider(edm);
		
		StmtIterator titles = proxy.listProperties(Dc.title);
		StmtIterator descriptions = proxy.listProperties(Dc.description);
		
		for(Statement titleSt: titles.toList()) {
			String titleStr = RdfUtil.getUriOrLiteralValue(titleSt.getObject());
			for(Statement descSt: descriptions.toList()) {
				String descStr = RdfUtil.getUriOrLiteralValue(descSt.getObject());
				if(descStr.length() > titleStr.length()) {
					LevenshteinDistance lev=new LevenshteinDistance();
					Integer dist = lev.apply(titleStr, descStr);
					
//					Option 1:
//						(basically the test for the pattern is levenshtein_distance(title, description)<min(length(title), length(description))
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
		
		return true;
	}
	
}
