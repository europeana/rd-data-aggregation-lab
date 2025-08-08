package europeana.rnd.dataprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;

public class MapOfIntsWithExamples<K, EX> {
	
	MapOfInts<K> mapOfInts;
	HashMap<K, Examples<EX>> examples;
	int sampleSize;
	
	public MapOfIntsWithExamples(int sampleSize) {
		mapOfInts=new MapOfInts<K>();
		examples=new HashMap<K, Examples<EX>>();
		this.sampleSize = sampleSize;
	}

	public void addTo(K key, EX example) {
		mapOfInts.incrementTo(key);
		Examples<EX> exs = examples.get(key);
		if(exs==null) {
			exs=new Examples<EX>(sampleSize);
			examples.put(key, exs);
		}
		exs.add(example);
	}

	public List<Entry<K, Integer>> getSortedEntries() {
		return mapOfInts.getSortedEntries(Sort.BY_KEY_ASCENDING);
	}

	public Set<EX> getSample(K key) {
		return examples.get(key).getSample();
	}
}
