package europeana.rnd.dataprocessing.scripts;

import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.EuropeanaDatasetProcessor;
import europeana.rnd.dataprocessing.EuropeanaDatasetProcessorHandler;
import inescid.dataaggregation.data.model.CreativeCommons;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.RdfUtil;

public class CountLicenseObjects extends EuropeanaDatasetProcessorHandler {
	int countLicenses=0;

	public static void main(String[] args) throws Exception {
		EuropeanaDatasetProcessor.run(args, new CountLicenseObjects());
	}

	@Override
	public boolean handleRecord(Model edm, int currentRecordIndex) {
		Set<Resource> licenses = RdfUtil.findResourcesOfType(edm, CreativeCommons.License);
		
		return true;
	}
	
}
