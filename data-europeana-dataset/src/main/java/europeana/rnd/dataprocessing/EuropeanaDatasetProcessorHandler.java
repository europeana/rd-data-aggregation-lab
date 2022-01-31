package europeana.rnd.dataprocessing;

import java.io.File;

import org.apache.jena.rdf.model.Model;

public abstract class EuropeanaDatasetProcessorHandler {
	File inputFolder = null;

	public void initParameters(String[] args) {
		if (args != null && args.length >= 1) {
				inputFolder = new File(args[0]);
		}else {
			inputFolder = new File("c://users/nfrei/desktop/data/europeana_dataset");
		}
	}

	public File getEuropeanaDatasetFolder() {
		return inputFolder;
	}

	public void closeProcessing() {
	}

	public abstract boolean handleRecord(Model edm, int currentRecordIndex);

	public void initProcessing() {
	}

	public File getWorkingFolder() {
		return null;
	}
}