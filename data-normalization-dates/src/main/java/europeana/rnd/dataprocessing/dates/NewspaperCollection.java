package europeana.rnd.dataprocessing.dates;

import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Dc;
import inescid.util.europeana.EdmRdfUtil;

public class NewspaperCollection {
	
	public enum NewspaperRecordType {
		ISSUE, TITLE
	}
	
	static HashSet<String> datasets=new HashSet<String>() {{
		add("9200338");
		add("15");
		add("2064915");
		add("9200204");
		add("0940443");
		add("9200236");
		add("0940423");
		add("9200355");
		add("2022091");
		add("9200360");
		add("2058617");
		add("0940427");
		add("9200333");
		add("0940416");
		add("9200302");
		add("9200384");
		add("9200375");
		add("9200115");
		add("9200396");
		add("08547");
		add("401");
		add("9200300");
		add("0940406");
		add("9200231");
		add("2021657");
		add("0940442");
		add("2020128");
		add("9200357");
		add("9200303");
		add("9200385");
		add("9200367");
		add("317");
		add("122");
		add("124");
		add("314");
		add("0940424");
		add("18");
		add("205");
		add("9200408");
		add("2058616");
		add("0940432");
		add("2022089");
		add("9200361");
		add("9200339");
		add("9200356");
		add("9200301");
		add("9200359");
		add("0940438");
		add("2021010");
		add("0940417");
	}};

	public static boolean isFromNewspapersCollection(String choUri) {
		String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
		return datasets.contains(dataset);
	}
	
	public static NewspaperRecordType identifyRecordType(Model choModel) {
		Resource proxyOfProvider = EdmRdfUtil.getProxyOfProvider(choModel);
		for(Statement typeSt : proxyOfProvider.listProperties(Dc.type).toList()) {
			if(typeSt.getObject().isLiteral()) {
				String val = typeSt.getObject().asLiteral().getString();
				if(val.equals("Newspaper Title"))
					return NewspaperRecordType.TITLE;
				if(val.equals("Newspaper Issue"))				
					return NewspaperRecordType.ISSUE;
			}
		}
		return null;
	}
}
