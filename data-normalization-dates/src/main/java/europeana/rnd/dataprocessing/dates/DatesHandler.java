package europeana.rnd.dataprocessing.dates;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;

public class DatesHandler {
	
	public static Set<Resource> edmClassesToProcess=new HashSet<Resource>() {{
		add(Ore.Proxy);
		add(Edm.Agent);
		add(Edm.WebResource);
		add(Edm.TimeSpan);
	}};
	
	class EntityTracker {
//		HashSet<String> processedInProvider=new HashSet<String>();
//		HashSet<String> processedInEuropeana=new HashSet<String>();
		HashSet<String> processed=new HashSet<String>();

		public boolean contains(Source source, String uri) {
			return processed.contains(uri);
		}
		
		public void add(Source source, String uri) {
			processed.add(uri);
		}
	}
	
	@SuppressWarnings("serial")
	private static HashSet<Property> temporalProperties = new HashSet<Property>() {
		{
			add(Dc.subject);
			add(Dc.coverage);
			add(Dc.date);
			add(DcTerms.subject);
			add(DcTerms.coverage);
			add(DcTerms.temporal);
			add(DcTerms.date);
			add(DcTerms.issued);
			add(DcTerms.available);
			add(DcTerms.created);
			add(DcTerms.dateAccepted);
			add(DcTerms.dateCopyrighted);
			add(DcTerms.dateSubmitted);
			add(DcTerms.modified);
			add(DcTerms.valid);
			add(Edm.begin);
			add(Edm.end);
			add(RdaGr2.dateOfBirth);
			add(RdaGr2.dateOfDeath);
			add(RdaGr2.dateOfEstablishment);
			add(RdaGr2.dateOfTermination);
		}
	};

	DatesJsonWriter jsonWriter;
	EntityTracker entityTracker=new EntityTracker();
	
	public DatesHandler(String outputFolder) {
		super();
		this.jsonWriter = new DatesJsonWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		DatesInRecord res = getDatesInRecord(edm);
		if (!res.isEmpty())
			jsonWriter.write(res);
//			if (recCnt % 10000 == 0) {
//			}
	}

	public DatesInRecord getDatesInRecord(Model edm) {
//			HashSet<String> processedResources=new HashSet<String>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		DatesInRecord res = new DatesInRecord(choUri);

		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			Source isEuropeanaProxy = europeanaProxySt != null
					&& europeanaProxySt.getObject().asLiteral().getBoolean() ? Source.EUROPEANA : Source.PROVIDER;
			for (Statement st : proxy.listProperties().toList()) {
				if (isTemporalProp(st.getPredicate())) {
					if (st.getObject().isLiteral()) {
						res.addTo(isEuropeanaProxy, Ore.Proxy, st.getPredicate(), st.getObject().asLiteral());
					} else if(st.getObject().isResource()) {
						Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
						if(typeSt!=null && typeSt.getObject().isResource()) {
							Resource resType=typeSt.getObject().asResource();
							if(edmClassesToProcess.contains(resType))
								getDatesInResource(res, isEuropeanaProxy, resType, st.getObject().asResource());
						}						
					}
				}
			}
		}
		return res;
	}

	private void getDatesInResource(DatesInRecord res, Source source, Resource resType, Resource resource) {
		if(resource.isURIResource()) {
			if(entityTracker.contains(source, resource.getURI()))
				return;
			entityTracker.add(source, resource.getURI());
		}
		for (Statement st : resource.listProperties().toList()) {
			if (isTemporalProp(st.getPredicate())) {
				if (st.getObject().isLiteral()) {
					res.addTo(source, resType, st.getPredicate(), st.getObject().asLiteral());
				} else if(st.getObject().isResource()) {
					Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
					if(typeSt!=null && typeSt.getObject().isResource()) {
						Resource subResType=typeSt.getObject().asResource();
						if(edmClassesToProcess.contains(resType))
							getDatesInResource(res, source, resType, st.getObject().asResource());
					}
				}
			}
		}
	}

	private static boolean isTemporalProp(Property predicate) {
		return temporalProperties.contains(predicate);
	}

	public void finalize() throws IOException {
		jsonWriter.close();
	}
}