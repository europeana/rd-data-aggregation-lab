package europeana.rnd.dataprocessing.dates.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class NewspapersIssuedStats {
	
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

	public void incrementIssue(String dataset) {
		statsGlobal.issuesCount++;
		getDatasetStats(dataset).issuesCount++;
	}

	public void incrementIssueWithDctermsIssued(String dataset) {
		statsGlobal.issuesWithDctermsIssued++;
		getDatasetStats(dataset).issuesWithDctermsIssued++;
	}

	public void incrementIssueWithDctermsIssuedNormalizable(String dataset) {
		statsGlobal.issuesWithDctermsIssuedNormalizable++;
		getDatasetStats(dataset).issuesWithDctermsIssuedNormalizable++;
	}

	public void incrementTitle(String dataset) {
		statsGlobal.titlesCount++;
		getDatasetStats(dataset).titlesCount++;
	}

	public int totalItems() {
		return statsGlobal.issuesCount + statsGlobal.titlesCount;
	}

	public List<String> sortedDatasets() {
		ArrayList<String> dss=new ArrayList<String>(statsByDataset.keySet());
		Collections.sort(dss);
		return dss;
	}
	
}
