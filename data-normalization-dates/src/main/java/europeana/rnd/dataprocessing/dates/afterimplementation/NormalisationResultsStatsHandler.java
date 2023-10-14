package europeana.rnd.dataprocessing.dates.afterimplementation;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import europeana.rnd.dataprocessing.dates.DatesHandler;
import europeana.rnd.dataprocessing.dates.ThematicCollections.NewspaperRecordType;
import europeana.rnd.dataprocessing.dates.afterimplementation.DatesInProviderRecord.Normalised;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.europeana.EdmRdfUtil;

public class DatesAfterImplementationHandler {
	
	public static class NormalisedChecker {
		MapOfInts<Property> datesInProviders=new MapOfInts<Property>();
		MapOfInts<Property> datesInEuropeana=new MapOfInts<Property>();
		
		public NormalisedChecker(Resource europeanaProxy, List<Resource> providersProxies) {
			for (Resource proxy : providersProxies) {
				for (Statement st : proxy.listProperties().toList()) {
					if (DatesHandler.isTemporalProp(st.getPredicate())) {
						if (st.getObject().isLiteral()) 
							datesInProviders.incrementTo(st.getPredicate());
					}
				}
			}

			for (Statement st : europeanaProxy.listProperties().toList()) {
				if (DatesHandler.isTemporalProp(st.getPredicate())) {
					if (st.getObject().isResource() && RdfUtil.getTypes(st.getObject().asResource()).get(0).equals(Edm.TimeSpan)) {
						System.out.println(st);
						Statement skosNotation = st.getObject().asResource().getProperty(Skos.notation);
						if(skosNotation!=null)
							datesInEuropeana.incrementTo(st.getPredicate());
					}
				}
			}
			
			
			System.out.println("in prov: "+datesInProviders.size()+"  - in Eur.: "+ datesInEuropeana.size());
			
			
		}
		
		public Normalised isNormalised(Property prop) {
			int inProviderCnt=datesInProviders.get(prop);
			int inEuropeanaCnt=datesInEuropeana.get(prop);
			
			if(inProviderCnt==0)
				throw new RuntimeException("data inconsistency. not in provider");
			if(inProviderCnt<inEuropeanaCnt)
				throw new RuntimeException("data inconsistency. too many in europeana");
			
			if (inProviderCnt==inEuropeanaCnt)
				return Normalised.TRUE;
			if(inEuropeanaCnt==0)
				return Normalised.FALSE;
			return Normalised.UNKNOWN;
		}
		
	}
	
	DatesInProviderRecordJsonWriter jsonWriter;
	
	public DatesAfterImplementationHandler(String outputFolder) {
		super();
		this.jsonWriter = new DatesInProviderRecordJsonWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		DatesInProviderRecord res = getDatesInRecord(edm);
		if (!res.isEmpty())
			jsonWriter.write(res);
//			if (recCnt % 10000 == 0) {
//			}
	}

	public static DatesInProviderRecord getDatesInRecordSingleRecord(Model edm) {
		return getDatesInRecord(edm);
	}

	private static DatesInProviderRecord getDatesInRecord(Model edm) {
//			HashSet<String> processedResources=new HashSet<String>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		
		Resource proxyOfEuropeana = EdmRdfUtil.getProxyOfEuropeana(edm);
		List<Resource> proxiesOfProviders = EdmRdfUtil.getProxiesOfProviders(edm);	

		NormalisedChecker normChecker=new NormalisedChecker(proxyOfEuropeana, proxiesOfProviders);
		
		DatesInProviderRecord res = new DatesInProviderRecord(choUri);
		for (Resource proxy : proxiesOfProviders) {
			for (Statement st : proxy.listProperties().toList()) {
				if (DatesHandler.isTemporalProp(st.getPredicate())) {
					if (st.getObject().isLiteral()) {
						res.add(st.getPredicate(), st.getObject().asLiteral(), normChecker.isNormalised(st.getPredicate()));
					} else if(st.getObject().isResource()) {
						res.add(st.getPredicate(), st.getObject().asResource());
					}
				}
			}
		}
		return res;
	}


	public void finalize() throws IOException {
		jsonWriter.close();
	}
}