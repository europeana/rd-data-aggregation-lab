package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.IOException;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import inescid.util.AccessException;
import inescid.util.datastruct.MapOfLists;

class SheetsFormWriter {
    	static {
    		GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
    	}
    	
		SheetsPrinter sheetsPrinter;
		
    	public SheetsFormWriter(String spreadsheetId, String sheetTitle) {
    		sheetsPrinter=new SheetsPrinter(spreadsheetId, sheetTitle);
		}
    	
    	public void init(String query) {
			sheetsPrinter.printRecord(
					"Object", "URI", "URL", "Detected class", "Confidence values/box", 
					"Correct and precise?", "Relevant?", "Merely acceptable, because", 
					"Missing relevant classes (from the SGoaB list)", "Notes", 
					"Iconclass tags available in the object's metadata (when applicable)", 
					 "", query==null ? "" : "Query - ", query==null ? "" : query
					);			
    	}
    	public void close() throws IOException {
    		sheetsPrinter.close();
    	}
    	
    	public void writeToEvaluationSheet(JsonAnnotatedCho cho, EdmMetadata edm) throws AccessException, InterruptedException, IOException {
//    		System.out.println("Writing to sheet: "+cho.toString());
    		MapOfLists<String, Annotation> groupedAnnotations=groupAnnotations(cho);
    		
    		boolean first=true;
    		for(String label: groupedAnnotations.keySet()) {
    			if(first) {
    				sheetsPrinter.print("=IMAGE(\""+edm.getImageUrl()+"\")", cho.choUri, edm.getImageUrl() );
    			} else
    				sheetsPrinter.print("", "", "" );
    			sheetsPrinter.print(label);
    			String confidences="";
    			for(Annotation anno: groupedAnnotations.get(label)) {
    				confidences+=anno.confidence+"("+anno.x+","+anno.y+","+anno.w+","+anno.h+") ";
    			}
    			sheetsPrinter.print(confidences.trim());
    			sheetsPrinter.print("", "", "", "", "");
    			if(first) {
	    			for(String[] subject: edm.getSubjects()) {
	    				sheetsPrinter.print("=HYPERLINK(\""+subject[0]+"\",\""+subject[1]+"\")");    				
	    			}
	    			first=false;
    			}    			
    			sheetsPrinter.println();
    		}
    	}

		private MapOfLists<String, Annotation> groupAnnotations(JsonAnnotatedCho cho) {
			MapOfLists<String, Annotation> groups=new MapOfLists<String, Annotation>();
			for(Annotation anno: cho.annotations) {
				groups.put(anno.label, anno);
			}			
			return groups;
		}
    }