package europeana.rnd.dataprocessing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class Examples<EX> {
	HashSet<EX> sample=new HashSet<EX>();
	Random random=new Random();
	int sampleSize=100;
	int totalFound=0;

	public Examples(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public void add(EX example) {
		totalFound++;
		if(!sample.contains(example)) {
			if(sample.size()<sampleSize)
				sample.add(example);
			else {
				if(random.nextFloat()<= 1/totalFound) { 
					removeRandomExample();
					sample.add(example);
				}
			}
		}
	}

	public HashSet<EX> getSample() {
		return sample;
	}

	private void removeRandomExample() {
		int randomNumber = random.nextInt(sample.size());
	    int currentIndex = 0;
		Iterator<EX> iterator = sample.iterator();
		while (iterator.hasNext()) {
	        EX randomElement = iterator.next();
	        if (currentIndex == randomNumber) {
	            sample.remove(randomElement);
	            return;
	        }
	        currentIndex++;
	    }
	}
}