package europeana.rnd.sparql;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import europeana.rnd.aggregated.FtpDownload;
import europeana.rnd.aggregated.RecordIteratorSingleDataset;
import europeana.rnd.aggregated.RecordIteratorSingleDataset.Handler;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient;
import inescid.util.europeana.EdmReg;
import inescid.util.europeana.SparqlClientOfEuropeana;

public class ScriptTestProductionDeploymentWithQueries {

	public static void main(String[] args) throws Exception {
		SparqlClient sparql=new SparqlClient("https://sparql.eanadev.org/sparql", EdmReg.nsPrefixes);
//		SparqlClient sparql=new SparqlClient("https://sparql.test.eanadev.org/sparql", EdmReg.nsPrefixes);
//		sparql.setDebug(true);
		
		String[] queries=new String[] {
			"PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT distinct(?DataProvider)\r\n"
			+ "WHERE { ?Aggregation edm:dataProvider ?DataProvider }",	

			"PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT DISTINCT ?Dataset\r\n"
			+ "WHERE {\r\n"
			+ "  ?Aggregation edm:datasetName ?Dataset ;\r\n"
			+ "      edm:country \"Italy\""
			+ "}",	
			
			"PREFIX ore: <http://www.openarchives.org/ore/terms/>\r\n"
			+ "PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT DISTINCT ?ProvidedCHO ?year\r\n"
			+ "WHERE {\r\n"
			+ "  ?Aggregation edm:aggregatedCHO ?ProvidedCHO ;\r\n"
			+ "      edm:country \"France\" .\r\n"
			+ "  ?Proxy ore:proxyFor ?ProvidedCHO ;\r\n"
			+ "      edm:year ?year .\r\n"
			+ "  FILTER (?year > \"1700\" && ?year < \"1800\")\r\n"
			+ "}\r\n"
			+ "ORDER BY asc(?year)\r\n",
//			+ "LIMIT 500",	
			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
			+ "PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT ?Agent\r\n"
			+ "WHERE { ?Agent rdf:type edm:Agent }\r\n",
//			+ "LIMIT 500"
			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
			+ "PREFIX ore: <http://www.openarchives.org/ore/terms/>\r\n"
			+ "PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT DISTINCT ?ProvidedCHO\r\n"
			+ "WHERE {\r\n"
			+ "  ?Place rdf:type edm:Place .\r\n"
			+ "  ?Proxy ?property ?Place ;\r\n"
			+ "      ore:proxyIn ?Aggregation .\r\n"
			+ "  ?Aggregation edm:aggregatedCHO ?ProvidedCHO\r\n"
			+ "}",
			
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
			+ "PREFIX ore: <http://www.openarchives.org/ore/terms/>\r\n"
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\r\n"
			+ "PREFIX edm: <http://www.europeana.eu/schemas/edm/>\r\n"
			+ "SELECT DISTINCT ?ProvidedCHO\r\n"
			+ "WHERE {\r\n"
			+ "  ?Concept rdf:type skos:Concept .\r\n"
			+ "  FILTER strstarts(str(?Concept), \"http://vocab.getty.edu/aat/\") .\r\n"
			+ "  ?Proxy ?property ?Concept ;\r\n"
			+ "      ore:proxyIn ?Aggregation .\r\n"
			+ "  ?Aggregation edm:aggregatedCHO ?ProvidedCHO\r\n"
			+ "}"
			+ "limit 10000",
			
			
		};
		
		int[] totals=new int[] {0, 0};
		
		Instant start=Instant.now();
		for(String q: queries) {
//			System.out.println(q);
			Instant qStart=Instant.now();
			final Instant[] qEnd=new Instant[1];
			
			sparql.query(q, new SparqlClient.Handler() {
				@Override
				public boolean handleSolution(QuerySolution solution) throws Exception {
					if(qEnd[0]==null)
						qEnd[0]=Instant.now();
						
					totals[0]++;
					for(Iterator<String> varNames = solution.varNames(); varNames.hasNext() ; ){
						String var = varNames.next();
						solution.get(var);
						totals[1]++;						
					}
					return true;
				}
			});
			System.out.println("Finished query "+totals[0]+" results");
			if(qEnd[0]!=null) {
				Duration d=Duration.between(qStart, qEnd[0]);
				System.out.println("Response time: " + d+" secs.");
			}
		}

		Instant now=Instant.now();
		Duration d=Duration.between(start, now);
		System.out.println("END: "+totals[0]+ " rows - "+totals[0]+" fields ; "+ d+" secs.");
		System.exit(0);
	}
}
