package europeana.rnd.dataprocessing.dates.stats;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import europeana.rnd.dataprocessing.dates.extraction.MatchId;

public class Examples {
	public HashSet<String> sample=new HashSet<String>();
	Random random=new Random();
	int sampleSize=100;
	public int totalFound=0;

	public Examples() {
	}
	
	public Examples(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public void add(String example) {
		totalFound++;
		if(!sample.contains(example)) {
			if(sample.size()<sampleSize)
				sample.add(example);
			else {
				if(random.nextFloat()<=0.1) { //add 1 every 100
					removeRandomExample();
					sample.add(example);
				}
			}
		}
	}

	public HashSet<String> getSample() {
		return sample;
	}

	public int getTotalFound() {
		return totalFound;
	}

	private void removeRandomExample() {
		int randomNumber = random.nextInt(sample.size());
	    int currentIndex = 0;
		Iterator<String> iterator = sample.iterator();
		while (iterator.hasNext()) {
	        String randomElement = iterator.next();
	        if (currentIndex == randomNumber) {
	            sample.remove(randomElement);
	            return;
	        }
	        currentIndex++;
	    }
	}
	
}
