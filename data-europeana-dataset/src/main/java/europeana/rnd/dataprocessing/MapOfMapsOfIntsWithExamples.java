package europeana.rnd.dataprocessing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import java.util.Random;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMaps;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfInts.Sort;

public class MapOfMapsOfIntsWithExamples<K1, K2, EX> {
	MapOfMapsOfInts<K1,K2> mapOfInts;
	MapOfMaps<K1, K2, Examples<EX>> examples;
	int sampleSize;
	
	public MapOfMapsOfIntsWithExamples(int sampleSize) {
		mapOfInts=new MapOfMapsOfInts<K1, K2>();
		examples=new MapOfMaps<K1, K2, Examples<EX>>();
		this.sampleSize = sampleSize;
	}

	public void addTo(K1 key1, K2 key2, EX example) {
		mapOfInts.incrementTo(key1, key2);
		Examples<EX> exs = examples.get(key1, key2);
		if(exs==null) {
			exs=new Examples<EX>(sampleSize);
			examples.put(key1, key2, exs);
		}
		exs.add(example);
	}

	public List<Entry<K2, Integer>> getSortedEntries(K1 key1, Sort sort) {
		return mapOfInts.get(key1).getSortedEntries(sort);//Sort.BY_KEY_ASCENDING
	}

	public Set<EX> getSample(K1 key1, K2 key2) {
		return examples.get(key1, key2).getSample();
	}

	public Set<K1> keySet() {
		return mapOfInts.keySet();
	}
}
