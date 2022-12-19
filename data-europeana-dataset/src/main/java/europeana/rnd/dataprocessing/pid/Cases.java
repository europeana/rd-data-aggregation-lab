package europeana.rnd.dataprocessing.pid;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import inescid.util.MapOfInts;

public class Cases {
	MapOfInts<String> sample=new MapOfInts<String>();
	int totalFound=0;
	
	public Cases() {
	}

	public void add(String example) {
		totalFound++;
		sample.incrementTo(example);
	}

	public List<Entry<String, Integer>> getCases() {
		return sample.getSortedEntries();
	}

	public int getTotalFound() {
		return totalFound;
	}


}
