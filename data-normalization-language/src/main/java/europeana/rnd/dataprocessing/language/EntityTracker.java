package europeana.rnd.dataprocessing.language;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface EntityTracker {
		public boolean contains(Source source, String uri);
		
		public boolean add(Source source, String uri);		
	}