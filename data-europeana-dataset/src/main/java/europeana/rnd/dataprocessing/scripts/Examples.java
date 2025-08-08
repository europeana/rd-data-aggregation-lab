package europeana.rnd.dataprocessing.scripts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;


public class Examples {
	class Example {
		String uri;
		String value;
		
		public Example(String uri, String value) {
			super();
			this.uri = uri;
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Example)
				return value.equals(value.equals( ((Example)obj).value));
			else if (obj instanceof String)
				return value.equals((String)obj);
			else
				return false;
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@Override
		public String toString() {
			return "\""+value+"\"@\""+uri+"\"";
		}
	}
	
	
	HashSet<Example> sample=new HashSet<Example>();
	Random random=new Random();
	int sampleSize=100;
	int totalFound=0;

	public Examples() {
	}
	
	public Examples(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public void add(String example, String uri) {
		totalFound++;
		if(!sample.contains(example)) {
			if(sample.size()<sampleSize)
				sample.add(new Example(uri, example));
			else {
				if(random.nextFloat()<=1/totalFound) { //add 1 every 100
					removeRandomExample();
					sample.add(new Example(uri, example));
				}
			}
		}
	}

	public HashSet<Example> getSample() {
		return sample;
	}

	public int getTotalFound() {
		return totalFound;
	}

	private void removeRandomExample() {
		int randomNumber = random.nextInt(sample.size());
	    int currentIndex = 0;
		Iterator<Example> iterator = sample.iterator();
		while (iterator.hasNext()) {
	        Example randomElement = iterator.next();
	        if (currentIndex == randomNumber) {
	            sample.remove(randomElement);
	            return;
	        }
	        currentIndex++;
	    }
	}
	
}
