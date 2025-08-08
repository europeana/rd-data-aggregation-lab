package europeana.rnd.sparql;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

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

public class ScriptTestProductionDeploymentForLossOfData {

	public static void main(String[] args) throws Exception {

//		http://www.europeana.eu/schemas/edm/		
//		String[] datasets=new String[] {"9200357", "222", "08547", "90901", "97", "842", "1021", "169", "2058703", "2059511", "394"};
//		String[] datasets=new String[] {"2048224"};
//		String[] datasets=new String[] {"91655"};
//		String[] datasets=new String[] {"91682"};
		String[] datasets=new String[] {"2063601"};
		
		File workingFolder=new File("c:/users/nfrei/desktop/SparqlTest");
		workingFolder.mkdir();
		FtpDownload ftp=new FtpDownload(workingFolder.getPath(), "XML");
		
		SparqlClient sparql=new SparqlClient("https://sparql.eanadev.org/sparql", EdmReg.nsPrefixes);
//		SparqlClient sparql=new SparqlClient("https://sparql.test.eanadev.org/sparql", EdmReg.nsPrefixes);
//		sparql.setDebug(true);
		
		String queryTemplate="SELECT (count(*) AS ?cnt) WHERE { GRAPH <http://data.europeana.eu/dataset/%s> {<%s> ?p ?o} }";

//		String q = String.format(queryTemplate, "dataset", "res.getURI()");
//		System.out.println(q);
//		System.exit(0);
		final int MAX_TESTS_PERDATASET=100000;
		int[] totals=new int[] {0,0,0,0};
		for(String dataset: datasets) {
			ftp.download(dataset);
			System.out.println("Testing dataset "+dataset);
			Model dsModel=Jena.createModel();

			
			Instant start=Instant.now();
			
			RecordIteratorSingleDataset it=new RecordIteratorSingleDataset(workingFolder);
			it.setLang(Lang.RDFXML);
			totals[1]=0;
			totals[3]=0;
			it.iterate(dataset, new Handler<Model, String>(){
				@Override
				public ProceedOption handle(Model recModel, String ds, String recId) throws Exception {
					for(Resource res: recModel.listSubjects().toList()) {
						if (!res.isURIResource() || res.getURI().startsWith("https://undefined.")) 
							continue;
						Resource type = RdfUtil.getTypeSingle(res);
						if(type==null || !type.equals(Edm.ProvidedCHO) && !type.equals(Edm.WebResource))
							continue;
						String q = String.format(queryTemplate, dataset, res.getURI());
						totals[0]++;
						totals[1]++;
						sparql.query(q, new SparqlClient.Handler() {
							@Override
							public boolean handleSolution(QuerySolution solution) throws Exception {
								Literal cnt = solution.getLiteral("cnt");
								if(cnt.getInt() != res.listProperties().toList().size()) {
									System.out.println("Statements difference found: in v;dl:"+cnt.getInt()+ ";" + res.listProperties().toList().size()+" ; "+res.getURI());
									totals[2]++;
									totals[3]++;
								}
								return true;
							}
						});
					}
					if(totals[1]>=MAX_TESTS_PERDATASET)
						return ProceedOption.STOP;
					if(totals[1] % 100 <= 5) {
						Instant now=Instant.now();
						Duration d=Duration.between(start, now);
						System.out.println("Progress: "+totals[1]+ " - "+d+" - "+((double)totals[1]/(double)d.getSeconds())+" recs/sec");
						
					}
						
					return ProceedOption.CONTINUE;
				}

				@Override
				public ProceedOption handleError(String error, Exception e) {
					System.err.println("ERROR ON QUERY# "+totals[0]);
					e.printStackTrace();
					return ProceedOption.STOP;
				}
			});
			System.out.println(dataset+" "+dsModel.size()+" triples");
			System.out.println("queries: "+totals[1]+ " errors: "+totals[3]);
		}
		System.out.println("EDN: queries: "+totals[0]+ " errors: "+totals[2]);
		System.exit(0);
	}
}
