package europeana.rnd.dataprocessing.dates;

import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Dc;
import inescid.util.europeana.EdmRdfUtil;

public class ThematicCollections {
	
	public enum NewspaperRecordType {
		ISSUE, TITLE
	}
	
	static HashSet<String> datasetsNewspaper=new HashSet<String>() {{
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

	static HashSet<String> datasetsArt=new HashSet<String>() {{
		add("2064108");
		add("285");
		add("2058611");
		add("91658");
		add("437");
		add("2058612");
		add("92062");
		add("90402");
		add("91619");
		add("180");
		add("2024908");
		add("2048211");
		add("2020903");
		add("2064909");
		add("22");
		add("15514");
		add("211");
		add("15508");
		add("2022717");
		add("199");
		add("643");
		add("9200579");
		add("2048128");
		add("2048001");
		add("2059209");
		add("916123");
		add("126");
		add("15501");
		add("91670");
		add("287");
		add("08547");
		add("9200518");
		add("2048221");
		add("366");
		add("2048005");
		add("2024914");
		add("2024903");
		add("2022096");
		add("2022362");
		add("92023");
		add("2048087");
		add("91625");
		add("89");
		add("2064116");
		add("2064401");
		add("2024909");
		add("188");
		add("168");
		add("440");
		add("463");
	}};
	

	static HashSet<String> datasetsWw1=new HashSet<String>() {{
		add("216");
		add("08611");
		add("9200522");
		add("2020601");
		add("9200178");
		add("9200204");
		add("9200236");
		add("9200291");
		add("9200360");
		add("9200197");
		add("9200354");
		add("2023863");
		add("08614");
		add("2059502");
		add("9200302");
		add("9200298");
		add("9200384");
		add("9200290");
		add("08618");
		add("236");
		add("08604");
		add("9200205");
		add("330");
		add("9200137");
		add("15601");
		add("9200214");
		add("2083601");
		add("9200231");
		add("9200518");
		add("09312");
		add("117");
		add("9200385");
		add("9200367");
		add("9200317");
		add("2023859");
		add("2024904");
		add("314");
		add("434");
		add("2022362");
		add("92023");
		add("2048087");
		add("18");
		add("9200199");
		add("9200521");
		add("9200312");
		add("9200140");
		add("9200361");
		add("9200196");
		add("08622");
		add("9200212");
	}};
	
	
	public static boolean isFromWw1Collection(String choUri) {
		String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
		return datasetsWw1.contains(dataset);
	}
	
	public static boolean isFromArtCollection(String choUri) {
		String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
		return datasetsArt.contains(dataset);
	}
	
	public static boolean isFromNewspapersCollection(String choUri) {
		String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
		return datasetsNewspaper.contains(dataset);
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
