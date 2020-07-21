package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Global;
import inescid.opaf.data.profile.MapOfInts;

public class CrawlResultStats extends CrawlResult {
	int seedsCount=0;

	public CrawlResultStats() {
		super();
	}

	public void addToStats(CrawlResult result) {
		try {
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(int.class)) {
					f.setInt(this, f.getInt(this) + CrawlResult.class.getDeclaredField(f.getName()).getInt(result));
				} else if (f.getType().equals(MapOfInts.class)) {
					MapOfInts<Resource> resultMap=(MapOfInts<Resource>)CrawlResult.class.getDeclaredField(f.getName()).get(result);					
					MapOfInts<Resource> statsMap = (MapOfInts<Resource>) f.get(this);
					for (Entry<Resource, Integer> classEntry : resultMap.entrySet()) {
						Resource uri = classEntry.getKey();
						Integer resultValue = classEntry.getValue();
						statsMap.addTo(uri, resultValue);
						
					}
				} else if (f.getType().equals(ArrayList.class)) {
					ArrayList<String> resultList=(ArrayList<String>)CrawlResult.class.getDeclaredField(f.getName()).get(result);					
					ArrayList<String> statsList = (ArrayList<String>) f.get(this);
					statsList.addAll(resultList);
				}
			}
			seedsCount++;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e.getMessage(), e); 
		}
	}
	
	@Override
	public byte[] serialize() throws IOException {
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		out.write(("seeds,"+(this.seedsCount)+"\n").getBytes("UTF8"));
		out.write(super.serialize());
		return out.toByteArray();
	}
	
	public int getCountValidRdf() {
//		return seedsCount-notFound-notRdf;
		return seedsCount-notRdf;
	}
}
