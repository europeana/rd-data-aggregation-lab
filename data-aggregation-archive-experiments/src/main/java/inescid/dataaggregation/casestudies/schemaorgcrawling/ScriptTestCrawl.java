package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.poi.hssf.util.HSSFColor.GOLD;

import com.drew.lang.ByteArrayReader;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.ld.RulesSchemaorgCrawlGraphOfCho;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.metadata.DatasetDescription;
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil;

public class ScriptTestCrawl {
	/* 
	 init componentes
	get URIs for sample
		call crawl on each URI
	write results
	--
		report of results:
		obtained resources #
		links not followed #
			links not followed by Class #
				
		irrelevant resources that were not mapped later.
		
	-- crawl of a URI
		getCho, 
			check Resorce class
			for each prop of cho 
				if prop is mappable, follow it if mappable to a resource. if just a reference use the URI. if literal, harvest linked resource and keep if schema:name is present
				    harvest the resource (if not just used as ref)
				    	if to be used as literal, keep if schema:name is present (may be further elaborated in future work)
				    	if resource check supported type. 
				    		if supported continue harvesting and apply resource harvest algorithm
				    		if not supported type, try to get a schema:name for use as literal, or discard.
	*/
	
	public static void main(String[] args) throws Exception {
		//experience workflow settings
		boolean reuseLastTestUris=true;
		boolean reuseLastCrawling=false;

		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		File urisFile = new File("target/schemaorg-test-uris.txt");
		
		String crawledTestUrisRepositoryDataset = "crawled-test-uris";

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		Repository dataRepository = Global.getDataRepository();
		SchemaOrgLodCrawler crawler=new SchemaOrgLodCrawler();

		if(!reuseLastCrawling)
			dataRepository.clear(crawledTestUrisRepositoryDataset);
			
		List<String> testUris = null;
		if(urisFile.exists() && urisFile.length()>0) {
			testUris = FileUtils.readLines(urisFile, Global.UTF8);
		} else {
			String dsKbAlba="http://data.bibliotheken.nl/id/dataset/rise-alba";
			testUris = new DatasetDescription(dsKbAlba).listRootResources();
			FileUtils.writeLines(urisFile, Global.UTF8.toString(), testUris);
		}			
		
		for(String uri : testUris) {
			CrawlResult crawlResult;
			if(reuseLastCrawling && dataRepository.contains(crawledTestUrisRepositoryDataset, uri)) {
				crawlResult = CrawlResult.deSerialize(dataRepository.getContent(crawledTestUrisRepositoryDataset, uri));				
			} else {
				crawlResult = crawler.crawlSchemaorgForCho(uri);
				dataRepository.save(crawledTestUrisRepositoryDataset, uri, crawlResult.serialize());
			}
			System.out.println(uri+" - "+new String(crawlResult.serialize()));
		};
	}
}
