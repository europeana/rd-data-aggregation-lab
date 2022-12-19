package inesc.util.testing;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import inescid.util.RdfUtil;

public class TestReadJsonLd {

	public static void main(String[] args) throws Exception {
//		Model m = RdfUtil.readRdf(new File("C:\\Users\\nfrei\\Desktop\\enrichment_collection_simple.json"), Lang.JSONLD);
		Model m = RdfUtil.readRdf(new File("C:\\Users\\nfrei\\Desktop\\enrichment_collection_complete.json"), Lang.JSONLD);
		RdfUtil.printOutRdf(m);
	}
}
