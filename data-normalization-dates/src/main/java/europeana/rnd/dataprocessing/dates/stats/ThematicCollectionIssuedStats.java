package europeana.rnd.dataprocessing.dates.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ThematicCollectionIssuedStats {
	
	
	DatesInIssuesStats statsGlobal=new DatesInIssuesStats();
	HashMap<String, DatesInIssuesStats> statsByDataset=new HashMap<String, DatesInIssuesStats>();
	
//	public DatesInIssuesStats getGlobalStats() {
//		return statsGlobal;
//	}

	private DatesInIssuesStats getDatasetStats(String dataset) {
		DatesInIssuesStats statsDataset=statsByDataset.get(dataset);
		if(statsDataset==null) {
			statsDataset=new DatesInIssuesStats();
			statsByDataset.put(dataset, statsDataset);
		}
		return statsDataset;
	}

	public void incrementRecord(String dataset) {
		statsGlobal.issuesCount++;
		getDatasetStats(dataset).issuesCount++;
	}

	public void incrementRecordWithDctermsIssued(String dataset) {
		statsGlobal.issuesWithDctermsIssued++;
		getDatasetStats(dataset).issuesWithDctermsIssued++;
	}

	public void incrementRecordWithDctermsIssuedNormalizable(String dataset) {
		statsGlobal.issuesWithDctermsIssuedNormalizable++;
		getDatasetStats(dataset).issuesWithDctermsIssuedNormalizable++;
	}

	public List<String> sortedDatasets() {
		ArrayList<String> dss=new ArrayList<String>(statsByDataset.keySet());
		Collections.sort(dss);
		return dss;
	}


	public int totalItems() {
		return statsGlobal.issuesCount + statsGlobal.titlesCount;
	}
	
}
