package europeana.rnd.dataprocessing.uri;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.FileWriterWithEncoding;

class UrisJsonWriter {
	static int maxRecsPerFile = 1000;
	static int maxFilesPerFolder = 1000;
//			final int maxRecsPerFile=20;
//			final int maxFilesPerFolder=10;

	String outputFolder;
	int folderCounter = 0;
	int fileCounter = 0;
	int recordCounter = 0;
	FileWriterWithEncoding writer;

	public UrisJsonWriter(String outputFolder) {
		super();
		this.outputFolder = outputFolder;
	}

	public void write(UrisInRecord res) throws IOException {
		if(res.isEmpty())
			return;
		if (writer == null) {
			fileCounter++;
			if (fileCounter == 1) {
				folderCounter++;
			}
			File folder = new File(outputFolder, "uris_export_" + String.format("%03d", folderCounter));
			if (!folder.exists())
				folder.mkdirs();
			writer = new FileWriterWithEncoding(new File(folder, "uris_export_" + String.format("%03d", folderCounter)
					+ "_" + String.format("%04d", fileCounter) + ".json"), StandardCharsets.UTF_8);
			writer.append("[\n");
		} else
			writer.append(",\n");
		writer.append(res.toJson().toString());
		recordCounter++;
		if (recordCounter == maxRecsPerFile) {
			writer.append("]");
			writer.close();
			writer = null;
			recordCounter = 0;
			if (fileCounter == maxFilesPerFolder)
				fileCounter = 0;
		}
	}

	public void close() throws IOException {
		if (writer != null) {
			writer.append("]");
			writer.close();
			writer = null;
		}
	}
}