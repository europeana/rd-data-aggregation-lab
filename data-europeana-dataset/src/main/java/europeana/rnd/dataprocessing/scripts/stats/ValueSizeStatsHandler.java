package europeana.rnd.dataprocessing.scripts.stats;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ebucore;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.europeana.EdmRdfUtil;

public class ValueSizeStatsHandler {
	
	public class ContentStats {
		MapOfInts<Integer> titleTextSize=new MapOfInts<Integer>();
		MapOfInts<Integer> descriptionTextSize=new MapOfInts<Integer>();
		MapOfInts<Integer> choTextSize=new MapOfInts<Integer>();
		MapOfInts<Integer> contextTextSize=new MapOfInts<Integer>();
		MapOfInts<Integer> webResourceTextSize=new MapOfInts<Integer>();
		MapOfInts<Integer> combinedTextSize=new MapOfInts<Integer>();
		
		private int recTitleCnt=0;
		private int recDescCnt=0;
		private int recChoCnt=0;
		private int recContextCnt=0;
		private int recWrCnt=0;
		
		public ContentStats() {
			titleTextSize.put(0, 0);
			descriptionTextSize.put(0, 0);
			choTextSize.put(0, 0);
			contextTextSize.put(0, 0);
			webResourceTextSize.put(0, 0);
			combinedTextSize.put(0, 0);
		}
		
		
		private void startRecord() {
			recTitleCnt=0;
			recDescCnt=0;
			recChoCnt=0;
			recContextCnt=0;
			recWrCnt=0;
		}
		public void endRecord() {
			titleTextSize.incrementTo(recTitleCnt);
			descriptionTextSize.incrementTo(recDescCnt);
			choTextSize.incrementTo(recChoCnt);
			contextTextSize.incrementTo(recContextCnt);
			webResourceTextSize.incrementTo(recWrCnt);
			combinedTextSize.incrementTo(recWrCnt+recContextCnt+recChoCnt);
			startRecord();
		}
		public void addToTitle(Literal l) {
			recTitleCnt+=l.getString().length();			
		}
		public void addToDescription(Literal l) {
			recDescCnt+=l.getString().length();			
		}
		public void addToCho(Literal l) {
			recChoCnt+=l.getString().length();			
		}
		public void addToContext(Literal l) {
			recContextCnt+=l.getString().length();			
		}
		public void addToWebResource(Literal l) {
			recWrCnt+=l.getString().length();			
		}
		
		private void writeCsvTo(File folder) throws IOException {
			writeCsvTo(folder, "1-");
			for(int range: new int[] {10, 20, 50, 100, 250, 500, 1000}) {
				ContentStats joined = joinByRange(range);
				joined.writeCsvTo(folder, range+"-");
			}
		}
		private void writeCsvTo(File folder, String filenamePrefix) throws IOException {
			StringBuilder sw=new StringBuilder();
			sw.append("text length, nr. of records\n");
			choTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING); 
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-cho.csv"), sw.toString(), StandardCharsets.UTF_8);
			sw=new StringBuilder();
			sw.append("title length, nr. of records\n");
			titleTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-title.csv"), sw.toString(), StandardCharsets.UTF_8);			
			sw=new StringBuilder();
			sw.append("description length, nr. of records\n");
			descriptionTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-description.csv"), sw.toString(), StandardCharsets.UTF_8);			
			sw=new StringBuilder();
			sw.append("text length, nr. of records\n");
			contextTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-context.csv"), sw.toString(), StandardCharsets.UTF_8);
			sw=new StringBuilder();
			sw.append("text length, nr. of records\n");
			webResourceTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-webresources.csv"), sw.toString(), StandardCharsets.UTF_8);
			sw=new StringBuilder();
			sw.append("text length, nr. of records\n");
			combinedTextSize.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			FileUtils.write(new File(outputFolder, filenamePrefix+"text-length-stats-combined.csv"), sw.toString(), StandardCharsets.UTF_8);			
		}
		
		public ContentStats joinByRange(int rangeSize) {
			ContentStats joined=new ContentStats();
			titleTextSize.forEach((len, count) -> {
				joined.titleTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			descriptionTextSize.forEach((len, count) -> {
				joined.descriptionTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			choTextSize.forEach((len, count) -> {
				joined.choTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			contextTextSize.forEach((len, count) -> {
				joined.contextTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			webResourceTextSize.forEach((len, count) -> {
				joined.webResourceTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			combinedTextSize.forEach((len, count) -> {
				joined.combinedTextSize.addTo(len==0 ? 0 : (int)Math.floor(len/rangeSize)*(rangeSize)+rangeSize-1, count);
			});
			return joined;
		}
		@Override
		public String toString() {
			return "ContentStats [recChoCnt=" + recChoCnt + ", recContextCnt=" + recContextCnt + ", recWrCnt="
					+ recWrCnt + "]";
		}
	}

	private static Set<Property> europeanaAddedPropertiesOfWebResources=new HashSet<Property>() {{
//		add(Ebucore.fileSize);
		add(Ebucore.fileByteSize);
		add(Ebucore.hasMimeType);
		add(Ebucore.width);
		add(Ebucore.height);
		add(Ebucore.orientation);
		add(Edm.componentColor);
		add(Edm.hasColorSpace);
		add(Edm.spatialResolution);
	}};
	
	
	ContentStats stats=new ContentStats();
	File outputFolder;
	
	public ValueSizeStatsHandler(String outputFolder) {
		super();
		this.outputFolder=new File(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
//		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		HashSet<Resource> entityTracker=new HashSet<Resource>();
		
		Resource proxy = EdmRdfUtil.getProxyOfDataProvider(edm);
		for (Statement st : proxy.listProperties().toList()) {
			if(st.getPredicate().equals(Rdf.type))
				continue;
			if(st.getObject().isLiteral()) {
				if(st.getPredicate().equals(Dc.title))
					stats.addToTitle(st.getObject().asLiteral());
				else if(st.getPredicate().equals(Dc.description))					
					stats.addToDescription(st.getObject().asLiteral());
			}
			if (st.getObject().isLiteral()) {
				stats.addToCho(st.getObject().asLiteral());
			} else if(st.getObject().isResource()) {
				processResource(st.getObject().asResource(), entityTracker);
			}
		}
		for (Resource webRes : EdmRdfUtil.getWebResourcesOfProviders(edm)) {
			for (Statement st : webRes.listProperties().toList()) {
				if(st.getPredicate().equals(Rdf.type))
					continue;
				if (st.getObject().isLiteral()) {
					if (! europeanaAddedPropertiesOfWebResources.contains(st.getPredicate())) {
						stats.addToWebResource(st.getObject().asLiteral());
					}
				} else if(st.getObject().isResource()) {
					processResource(st.getObject().asResource(), entityTracker);
				}
			}
		}
		stats.endRecord();
	}
	
	
	private void processResource(Resource resource, HashSet<Resource> entityTracker) {
		Resource rdfType = RdfUtil.getTypeSingle(resource);
		if(rdfType==null || rdfType.equals(Edm.WebResource) || rdfType.equals(Edm.ProvidedCHO) || rdfType.equals(Ore.Proxy) || rdfType.equals(Ore.Aggregation) || rdfType.equals(Edm.EuropeanaAggregation))
			return;
		if(entityTracker.contains(resource))
			return;
		entityTracker.add(resource);
		for (Statement st : resource.listProperties().toList()) {
			if(st.getPredicate().equals(Rdf.type))
				continue;
			if (st.getObject().isLiteral()) {
				stats.addToContext(st.getObject().asLiteral());
			} else if(st.getObject().isResource()) {
				processResource(st.getObject().asResource(), entityTracker);
			}
		}
	}

	public void finalize() throws IOException {
		if (!outputFolder.exists())
			outputFolder.mkdirs();
		stats.writeCsvTo(outputFolder);
	}
}