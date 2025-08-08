package europeana.rnd.sparql;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class ScriptTestProductionDeploymentForLossOfDataFullDataset {

  public static class DatasetChecks {
    String dataset;
    int chosInDownloads;
    int chosInSparql;
    int proxiesInDownloads;
    int proxiesInSparql;
    int webResInDownloads;
    int webResInSparql;
    
    HashSet<String> webResUris=new HashSet();

    public DatasetChecks(String dataset) {
      super();
      this.dataset = dataset;
    }

    Pattern pCho=Pattern.compile("<edm:ProvidedCHO ([^>]*)>");
    Pattern pWr=Pattern.compile("<edm:WebResource rdf:about=\"([^\"]+)\"");
//    Pattern pWr=Pattern.compile("<edm:WebResource ([^>]*)>");
    Pattern pProxy=Pattern.compile("<ore:Proxy[ /]([^>]*)>");
    public void addRecord(String rdfString) {
        Matcher m=pCho.matcher(rdfString);
        while(m.find())
          chosInDownloads++;
        m=pWr.matcher(rdfString);
        while(m.find()) {
          webResInDownloads++;
          System.out.println(m.group(1));
          webResUris.add(m.group(1));
        }
        m=pProxy.matcher(rdfString);
        while(m.find())
          proxiesInDownloads++;
    }

    @Override
    public String toString() {
      StringBuilder sb=new StringBuilder();
      sb.append(dataset).append("\n");
      sb.append(String.format("CHOs - dnlds:%d - sparql:%d - diff:%d\n", chosInDownloads, chosInSparql, chosInDownloads - chosInSparql));
//      sb.append(String.format("WRs - dnlds:%d(unique %d) - sparql:%d - diff:%d\n", webResInDownloads, webResUris.size(),  webResInSparql, webResInDownloads - webResInSparql));
      int wrCntDownloads=webResUris.size() == 0 ? webResInDownloads : webResUris.size();
      sb.append(String.format("WRs - dnlds:%d - sparql:%d - diff:%d\n", wrCntDownloads,  webResInSparql, wrCntDownloads - webResInSparql));
      sb.append(String.format("Prxs - dnlds:%d - sparql:%d - diff:%d\n", proxiesInDownloads, proxiesInSparql, proxiesInDownloads - proxiesInSparql));
      return sb.toString();
    }

    /**
     * @return
     */
    public boolean hasDifferences() {
//      return chosInDownloads!=chosInSparql || webResInDownloads!=webResInSparql || proxiesInDownloads!=proxiesInSparql;
      int wrCntDownloads=webResUris.size() == 0 ? webResInDownloads : webResUris.size();
      return chosInDownloads!=chosInSparql || wrCntDownloads!=webResInSparql || proxiesInDownloads!=proxiesInSparql;
    }

    /**
     * @param dsCheck
     */
    public void addSubdataset(DatasetChecks dsCheck) {
      chosInDownloads+=dsCheck.chosInDownloads;
      chosInSparql+=dsCheck.chosInSparql;
//      webResInDownloads+=dsCheck.webResInDownloads;
      webResInDownloads+=dsCheck.webResUris.size() == 0 ? dsCheck.webResInDownloads : dsCheck.webResUris.size();
      webResInSparql+=dsCheck.webResInSparql;
      proxiesInDownloads+=dsCheck.proxiesInDownloads;
      proxiesInSparql+=dsCheck.proxiesInSparql;
    }
    
    
  }

  public static void main(String[] args) throws Exception {
//		http://www.europeana.eu/schemas/edm/		
    File workingFolder = new File("c:/users/nfrei/desktop/SparqlTest");
    String sparqlUrl = "https://sparql.eanadev.org/sparql";
//	  String sparqlUrl="https://sparql.test.eanadev.org/sparql";
    if (args != null && args.length >= 1) {
      workingFolder = new File(args[0]);
      if (args.length >= 2) {
        sparqlUrl = args[1];
      }
    }
    if (!workingFolder.exists())
      workingFolder.mkdir();
    FtpDownload ftp = new FtpDownload(workingFolder.getPath(), "XML");

    SparqlClient sparql = new SparqlClient(sparqlUrl, EdmReg.nsPrefixes);
//		sparql.setDebug(true);

    String queryTemplate = "SELECT (count(distinct(?s)) AS ?cnt) WHERE { GRAPH <http://data.europeana.eu/dataset/%s> {?s a %s} }";

//		String q = String.format(queryTemplate, "dataset", "res.getURI()");
//		System.out.println(q);
//		System.exit(0);
    Map<String, DatasetChecks> dsChecks = new HashMap<>();
    DatasetChecks globalCheck = new DatasetChecks("Europeana.eu Dataset");
    for (File file : workingFolder.listFiles()) {
      if (!file.getName().endsWith(".zip"))
        continue;
      String dataset = file.getName().substring(0, file.getName().indexOf('.'));
      System.out.println("Checking dataset " + dataset);
      DatasetChecks dsCheck = new DatasetChecks(dataset);
      dsChecks.put(dataset, dsCheck);
      Instant start = Instant.now();

      RecordIteratorSingleDataset it = new RecordIteratorSingleDataset(workingFolder);
      it.setLang(Lang.RDFXML);
      it.iterateRdfString(dataset, new Handler<String, String>() {
        @Override
        public ProceedOption handle(String recRdf, String ds, String recId) throws Exception {
          dsCheck.addRecord(recRdf);
          if (dsCheck.chosInDownloads % 100000 == 0) {
            Instant now = Instant.now();
            Duration d = Duration.between(start, now);
            System.out.println("Progress: " + dsCheck.chosInDownloads + " - " + d + " - "
                + ((double) dsCheck.chosInDownloads / (double) d.getSeconds()) + " recs/sec");
          }
          return ProceedOption.CONTINUE;
        }

        @Override
        public ProceedOption handleError(String error, Exception e) {
          System.out.println("ERROR: " + error);
          e.printStackTrace(System.out);
          return ProceedOption.STOP;
        }
      });

      for (String rdfClass : new String[] { "edm:ProvidedCHO", "ore:Proxy", "edm:WebResource" }) {
        String q = String.format(queryTemplate, dataset, rdfClass);
        final int[] cnt = new int[] { 0 };
        try {
          sparql.query(q, new SparqlClient.Handler() {
            @Override
            public boolean handleSolution(QuerySolution solution) throws Exception {
              Literal cntLit = solution.getLiteral("cnt");
              cnt[0] = cntLit.getInt();
              return true;
            }
          });
          if (rdfClass.equals("edm:ProvidedCHO"))
            dsCheck.chosInSparql = cnt[0];
          else if (rdfClass.equals("ore:Proxy"))
            dsCheck.proxiesInSparql = cnt[0];
          else if (rdfClass.equals("edm:WebResource"))
            dsCheck.webResInSparql = cnt[0];
          else
            throw new RuntimeException("Should not happen");
        } catch (Exception e) {
          System.out.println("Error in " + q);
          e.printStackTrace(System.out);
        }
      }
      globalCheck.addSubdataset(dsCheck);
      System.out.println(dataset + " FINISHED:\n" + dsCheck.toString());
    }
    printResult(globalCheck, dsChecks);
  }

  /**
   * @param globalCheck
   * @param dsChecks
   */
  private static void printResult(DatasetChecks globalCheck, Map<String, DatasetChecks> dsChecks) {
    System.out.println("### Final report ###");
    System.out.println(globalCheck);
    int dsDifs=0;
    int dsOk=0;
    for(String ds: dsChecks.keySet()) {
      DatasetChecks dsChk = dsChecks.get(ds);
      if(dsChk.hasDifferences())
        dsDifs++;
      else
        dsOk++;
    }
    System.out.println("Datasets without differences: "+dsOk);
    System.out.println("Datasets with differences: "+dsDifs);
    System.out.println("Details:");
    for(String ds: dsChecks.keySet()) {
      DatasetChecks dsChk = dsChecks.get(ds);
      if(dsChk.hasDifferences())
        System.out.println(dsChk);
    }
    
    
  }
}
