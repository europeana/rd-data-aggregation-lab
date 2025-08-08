package europeana.rnd.aggregated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIteratorSingleDataset.Handler.ProceedOption;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class RecordIteratorSingleDataset {
	public static final int EUROPEANA_DATASET_SIZE=60000000; 

	public interface Handler<TYPE, ERROR_TYPE> {
		public enum ProceedOption { CONTINUE, STOP, GO_TO_NEXT_DATASET }	
		
		public ProceedOption handle(TYPE resource, String dataset, String recId) throws Exception;
		public ProceedOption handleError(ERROR_TYPE error, Exception e);
	}

	
	class Producer {
		BlockingQueue<Model> queue=new ArrayBlockingQueue<Model>(10);
		boolean finished=false;
		boolean goToNextDataset=false;
		Thread prodThread;
		
		protected void start(String dataset, Handler<Model, String> userHandler) {
			prodThread=new Thread(
					new Runnable() {
						@Override
						public void run() {
							try {
								iterateRecords(dataset, new Handler<Model, String>(){
									@Override
									public ProceedOption handle(Model resource, String dataset, String recId) throws Exception {
										if(goToNextDataset) {
//											queue.clear();
											goToNextDataset=false;
											return ProceedOption.GO_TO_NEXT_DATASET;
										}
										queue.put(resource);
//										System.out.println("produced "+queue.size());
										return ProceedOption.CONTINUE;
									}

									@Override
									public ProceedOption handleError(String error, Exception e) {
										return userHandler.handleError(error, e);
									}
								}
								);
							} catch (Exception e) {
								System.err.println("Error reading Europeana datadump");
								e.printStackTrace();
//							} catch (IOException e) {
//								System.err.println("Error reading Europeana datadump");
//								e.printStackTrace();
							}finally {
//								System.out.println("Prod: I am finished.");
								finished=true;								
							}
//							System.out.println("Producer finished");
						}
					} );
			prodThread.start();
		}
		
		public boolean isFinished() {
			return finished;
		}
		
		protected void close() throws InterruptedException {
			if(!finished && prodThread.isAlive())
				prodThread.interrupt();
			prodThread.join();
		}

		public Model next() throws InterruptedException {
//				System.out.println("finished? "+finished);
//			Model take = queue.take();
//				System.out.println("polling");
				while(!finished) {
					Model take = queue.poll(5, TimeUnit.SECONDS);
//					System.out.println("polling out");
//					System.out.println("finished? "+finished);
//					System.out.println("is null "+ (take==null));
					
	//			System.out.println("consuming "+queue.size());
					if(take!=null)
						return take;
				}
				return null;
		}

		public void goToNextDataset() {
//			System.out.println("Goto next ds");
			goToNextDataset=true;
		}
	}
	
	
	
	File repositoryFolder;
	int startAt=0;
	Lang lang=Lang.TURTLE;
	int recIndex=0;
	
	public RecordIteratorSingleDataset(File repositoryFolder) {
		super();
		this.repositoryFolder = repositoryFolder;
		if(!repositoryFolder.exists())
			throw new RuntimeException("Dumps folder not found at "+ repositoryFolder.getAbsolutePath());
	}
	
	public void setStartRecord(int zeroBasedIndex) {
		startAt=zeroBasedIndex;
	}
	
	
	public void iterate(String dataset, Handler<Model, String> handler) throws Exception {
		Producer prod=new Producer();
		prod.start(dataset, handler);
		ProceedOption handlerContinues=ProceedOption.CONTINUE;
		while(!prod.isFinished()) {
			if (handlerContinues==ProceedOption.CONTINUE) {
//				System.out.println("prod next");
				Model m=prod.next();
				if(m!=null) {
					String recUri=EdmRdfUtil.getProvidedChoResource(m).getURI();
					int slashIdx=recUri.indexOf('/');
					handlerContinues = handler.handle(m, recUri.substring(0, slashIdx), recUri.substring(slashIdx + 1) );
				} else {
					System.out.println("WARN - RecordIterator: null model receive from producer");
					handlerContinues=ProceedOption.CONTINUE;
				}
			} else if (handlerContinues==ProceedOption.GO_TO_NEXT_DATASET) { 
				prod.goToNextDataset();
//				System.out.println("prod next ds");
				Model nextModel=prod.next();
				if(nextModel!=null) {
					String recUri=EdmRdfUtil.getProvidedChoResource(nextModel).getURI();
					int slashIdx=recUri.indexOf('/');
					handlerContinues = handler.handle(nextModel, recUri.substring(0, slashIdx), recUri.substring(slashIdx + 1));				
				}
			}else
				break;
//			System.out.println("prod next out");			
		}
//		System.out.println("closing producer");
		prod.close();
	}
	
	public void iterateRdfString(String dataset, Handler<String, String> handler) throws Exception {
		String[] allFiles=repositoryFolder.list();
		Arrays.sort(allFiles);
		boolean normalize=lang.equals(Lang.RDFXML);
		DATASET_FILE: for(String filename: allFiles) {
			String datasetOfFile=filename.substring(0, filename.indexOf('.'));
			if(!datasetOfFile.equals(dataset))
			  continue;
			FileInputStream zipFileInputStream = new FileInputStream(new File(repositoryFolder, filename));
			final ZipInputStream zip = new ZipInputStream(zipFileInputStream);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				if(recIndex>=startAt) {
					String edmRdfXml="";
					try {
						String recId=entry.getName().substring(0, entry.getName().indexOf('.'));
						edmRdfXml=IOUtils.toString(zip, StandardCharsets.UTF_8);
						if(normalize) {
							edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
							edmRdfXml=EdmRdfUtil.escapeSpacesInUrisOfRdf(edmRdfXml);	
						}
						ProceedOption handlerResult = handler.handle(edmRdfXml, dataset, recId);
						if(handlerResult==ProceedOption.STOP) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						} else if(handlerResult==ProceedOption.GO_TO_NEXT_DATASET) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							continue DATASET_FILE;
						}
					} catch (Exception e) {
						ProceedOption handlerResult = handler.handleError(entry.getName() ,e);
						if(handlerResult==ProceedOption.STOP) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						} else if(handlerResult==ProceedOption.GO_TO_NEXT_DATASET) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							continue DATASET_FILE;
						}
					} catch (Throwable e) {
						zip.closeEntry();
						zip.close();
						zipFileInputStream.close();
						throw e;
					}
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
				recIndex++;
			}
			zip.close();
			break;
		}
	}
	
	private void iterateRecords(String dataset, Handler<Model, String> handler) throws IOException {
		String[] allFiles=repositoryFolder.list();
		Arrays.sort(allFiles);

		boolean normalize=lang.equals(Lang.RDFXML);
		
			String filename=dataset+".zip";
			FileInputStream zipFileInputStream = new FileInputStream(new File(repositoryFolder, filename));
			final ZipInputStream zip = new ZipInputStream(zipFileInputStream);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				if(recIndex>=startAt) {
					String edmRdfXml="";
					try {
						String recId=entry.getName().substring(entry.getName().indexOf('.'));
						edmRdfXml=IOUtils.toString(zip, StandardCharsets.UTF_8);
						if(normalize)
							edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
						Model readRdf = RdfUtil.readRdf(edmRdfXml, lang);
						ProceedOption handlerResult = handler.handle(readRdf, dataset, recId);
						if(handlerResult==ProceedOption.STOP) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						} else if(handlerResult==ProceedOption.GO_TO_NEXT_DATASET) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							break;
						}
					} catch (Exception e) {
						ProceedOption handlerResult = handler.handleError(entry.getName() ,e);
						if(handlerResult==ProceedOption.STOP) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						} else if(handlerResult==ProceedOption.GO_TO_NEXT_DATASET) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							break;
						}
					} catch (Throwable e) {
						zip.closeEntry();
						zip.close();
						zipFileInputStream.close();
						System.err.println("Unexpected exception: ");
						e.printStackTrace();
						throw e;
					}
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
				recIndex++;
			}
			zip.close();
		System.out.println("All datasets processed");
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

	public int getCurrentRecordIndex() {
		return recIndex;
	}
	
	
}
