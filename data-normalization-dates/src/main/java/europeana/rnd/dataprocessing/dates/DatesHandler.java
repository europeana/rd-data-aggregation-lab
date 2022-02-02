package europeana.rnd.dataprocessing.dates;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

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
	@SuppressWarnings("serial")
	private static HashSet<Property> temporalProperties = new HashSet<Property>() {
		{
//			add(Dc.subject);
//			add(Dc.coverage);
			add(Dc.date);
//			add(DcTerms.subject);
//			add(DcTerms.coverage);
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

	public static DatesInRecord getDatesInRecord(Model edm) {
//			HashSet<String> processedResources=new HashSet<String>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		DatesInRecord res = new DatesInRecord(choUri);

		for (Resource resType : new Resource[] { Ore.Proxy, Ore.Aggregation, Edm.WebResource, Edm.Agent, Edm.Place,
				Edm.TimeSpan }) {
			for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, resType).toList()) {
//				processedResources.add(RdfUtil.getUriOrId(proxy));
				for (Statement st : proxy.listProperties().toList()) {
					if (st.getObject().isLiteral()) {
						if (isTemporalProp(st.getPredicate())) {
							if (resType.equals(Edm.WebResource))
								res.addToWebResources(st.getPredicate(), st.getObject().asLiteral());
							else if (resType.equals(Edm.Agent))
								res.addToAgents(st.getPredicate(), st.getObject().asLiteral());
							else if (resType.equals(Edm.TimeSpan))
								res.addToTimeSpans(st.getPredicate(), st.getObject().asLiteral());
							else if (resType.equals(Ore.Proxy)) {
								Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
								boolean isEuropeanaProxy = europeanaProxySt != null
										&& europeanaProxySt.getObject().asLiteral().getBoolean();
								if (isEuropeanaProxy)
									res.addToEuropeanaProxy(st.getPredicate(), st.getObject().asLiteral());
								else
									res.addToProviderProxy(st.getPredicate(), st.getObject().asLiteral());
							} else if (resType.equals(Ore.Aggregation)) {
								System.out.println("WARNING: temporal prop in ore:Aggregation - " + st);
							} else if (resType.equals(Edm.ProvidedCHO)) {
								System.out.println("WARNING: temporal prop in edm:ProvidedCHO - " + st);
							}
						}
					}
				}
			}

		}
		return res;
	}

//		public static DatesInRecord getDatesInRecord(Model edm) {
//			HashSet<String> processedResources=new HashSet<String>();
//			Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
//			String choUri = choRes.getURI();
//			DatesInRecord res=new DatesInRecord(choUri);
//			for(Resource proxy:edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
//				processedResources.add(RdfUtil.getUriOrId(proxy));
//				Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
//				boolean isEuropeanaProxy=europeanaProxySt!=null && europeanaProxySt.getObject().asLiteral().getBoolean();
//				for(Statement st: proxy.listProperties().toList()) {
//					if(st.getObject().isResource()) {
//						processInnerResource(res, st.getObject().asResource(), processedResources);
//					}else if(st.getObject().isLiteral()) {
//						if(isTemporalProp(st.getPredicate())) {
//							if(isEuropeanaProxy)
//								res.addToEuropeanaProxy(st.getPredicate(), st.getObject().asLiteral());
//							else
//								res.addToProviderProxy(st.getPredicate(), st.getObject().asLiteral());
//						}
//					}
//				}
//			}
//			Resource aggRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Ore.Aggregation, null, null);
//			processedResources.add(RdfUtil.getUriOrId(aggRes));
//			for(Statement st: aggRes.listProperties().toList()) {
//				if(st.getObject().isResource()) {
//					processInnerResource(res, st.getObject().asResource(), processedResources);
//				}else if(st.getObject().isLiteral()) {
//					//TODO: should properties in aggregation be processed?
//					if(isTemporalProp(st.getPredicate())) 
//						System.out.println("WARNING: temporal prop in ore:Aggregation - "+st);
//				}
//			}
//			return res;
//		}

	private static boolean isTemporalProp(Property predicate) {
		return temporalProperties.contains(predicate);
	}

	public void finalize() throws IOException {
		jsonWriter.close();
	}
//
//		private static void processInnerResource(DatesInRecord res, Resource resource, HashSet<String> processedResources) {
//			if(processedResources.contains(RdfUtil.getUriOrId(resource)))
//				return;
//			processedResources.add(RdfUtil.getUriOrId(resource));
//			
//			List<Resource> types = RdfUtil.getTypes(resource);
//			if(types==null || types.isEmpty()) return;
//			Resource type=null;
//			if(types.contains(Edm.WebResource))
//				type=Edm.WebResource;
//			else if(types.contains(Edm.Agent))
//				type=Edm.Agent;
//			else if(types.contains(Edm.TimeSpan))
//				type=Edm.TimeSpan;
//			else
//				return;
//			for(Statement st: resource.listProperties().toList()) {
//				if(st.getObject().isResource()) {
//					processInnerResource(res, st.getObject().asResource(), processedResources);
//				}else if(st.getObject().isLiteral()) {
//					if(isTemporalProp(st.getPredicate())) {
//						if(type.equals(Edm.WebResource))
//							res.addToWebResources(st.getPredicate(), st.getObject().asLiteral());
//						else if(type.equals(Edm.Agent))
//							res.addToAgents(st.getPredicate(), st.getObject().asLiteral());
//						else if(type.equals(Edm.TimeSpan))
//							res.addToTimeSpans(st.getPredicate(), st.getObject().asLiteral());
//					}
//				}
//			}
//		}
}