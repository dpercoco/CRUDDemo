package crud.JAVA;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import net.sourceforge.tess4j.Tesseract;

import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.exception.TikaException;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;

import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.sax.WriteOutContentHandler;

import org.xml.sax.SAXException;

public class ConvertFile {
	    
	    private static String content="";
	    private static File file;
	    private static final int PDF =1;
	    private static final int MSWORD =2;
	    private static int docType=0;	    
	    private static String FILE_INPUT = "C:\\Sample.pdf";
	    private static String FOLDER_OF_PDFS="";  
	    private static String FOLDER_OF_TEXTS="";
	    private static String contents[];
	    private static String fileNames[];
	    
	    public ConvertFile(String IN_file) throws IOException, SAXException, TikaException {
	    	FILE_INPUT = IN_file;
	    	file = new File(FILE_INPUT);
	    	extractContent();
		}
	    
	  
	    public ConvertFile(File f) throws IOException, SAXException, TikaException {
	    	FILE_INPUT = f.getPath();
	    	file = f;
	    	extractContent();
		}


		public static void main(String[] args) throws IOException, SAXException, TikaException {
	    	
	    	String usage = "java -jar C:\\convertFiles.jar FILE_NAME [e.g. C:\\MyFolder\\MyFile.pdf]"
	            + "\n\nDesc: This program converts PDF, DOC and RTF files into text files. \n"
	            + "The text file is created in the same directoy as the input file. ";
	    	
	    	if (args.length == 0) {
	   	      	System.err.println("Usage: " + usage);
	   	      	System.exit(1);
	   	    }
	    	
	    	if (args.length> 0) {
	    		FILE_INPUT = args[0];
	    		file = new File(FILE_INPUT);
		    	extractContent();
		    	saveTextFile();
	    	}	    	
	    }
	    
	    private static void process_FOLDER_OF_PDFS() throws IOException {		
			
			File dir = new File(FOLDER_OF_PDFS);
			File[] files = dir.listFiles();	
			contents = new String[files.length];
			fileNames = new String[files.length];
			int pdfCount=0;
			
			try {	   
		    	for (File inFile : files) {    			
					if (inFile.isFile()) {
						file = new File(inFile.getPath());
				    	extractContent();
				    	contents[pdfCount]=content;
				    	fileNames[pdfCount]=file.getPath();
						pdfCount+=1;
		    		}   
		    	}
	        }
	        catch (Exception e){
	        	System.out.println("\n Server Error while reading files." );
	         } 
	        
	        //System.out.println("File count: " + pdfCount);
		}

	    private static void process_FOLDER_OF_TEXTS() throws IOException {
	    
	    	File dir = new File(FOLDER_OF_TEXTS);
			File[] files = dir.listFiles();	
			contents = new String[files.length];
			fileNames = new String[files.length];
			int ct=0;
			FileInputStream fis;
			String strLine;
			
			try {	   
		    	for (File inFile : files) {    			
					if (inFile.isFile()) {
						file = new File(inFile.getPath());
						fis = new FileInputStream(file);
						DataInputStream in = new DataInputStream(fis);
						//BufferedReader br = new BufferedReader(new InputStreamReader(in)); // ISO-8859-1,UTF-8
						BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("ISO-8859-1"))); 
				        content = "";
						while ((strLine = br.readLine()) != null)  { 
							content += strLine + "\n";
						}
				    	contents[ct]=content;
				    	fileNames[ct]=file.getPath();
						ct+=1;
						br.close(); 
						fis.close();
		    		}   
		    	}
	        }
	        catch (Exception e){
	        	System.out.println("\n Server Error while reading files." );
	         }
		}
	    
	    private static void extractContent () throws IOException, SAXException, TikaException {
	        
	    	 try {
	    		if (file.getPath().toLowerCase().indexOf(".pdf")>-1) docType = PDF;	    	
	 	    	if (file.getPath().toLowerCase().indexOf(".doc")>-1) docType = MSWORD;
	 	    	if (file.getPath().toLowerCase().indexOf(".rtf")>-1) docType = MSWORD;
	 	    	
	 	    	switch (docType) {
	 	            case PDF:  parsePDF();
	 	                 break;
	 	            case MSWORD:  parseMSWord();
	                 	break;
	 	    	}
                     
	                if (content.trim().length()>0) {
	                    OCR(file.getPath());
	                }
	        } catch (FileNotFoundException E) {
	             System.out.println("No such file : " + file.getPath());
	        }
	    	   	
	    }
	    
	 public static void saveTextFile() throws FileNotFoundException, UnsupportedEncodingException {
	    	
	    	String FILE_OUT = "C:\\Sample.txt";
	    	int foundIndx = file.getPath().lastIndexOf(".");
	    	if (foundIndx>-1) {
	    		FILE_OUT = file.getPath().substring(0,foundIndx+1) + "txt";
	    	}
	    	PrintWriter writer = new PrintWriter(FILE_OUT, "UTF-8");
	    	System.out.println("\nCreated file:" + FILE_OUT);
	    	writer.println(content);
	    	writer.close();
	    }
	   
	 private static void parsePDF() throws IOException {
	    	
	    	PDDocument pdfdoc = PDDocument.load(file);
	    	PDFTextStripper stripper = new PDFTextStripper();
	    	content = null;
	    	try {
	    		content = stripper.getText(pdfdoc);
		    }
    		catch (Exception e){
    		}
    		pdfdoc.close();    		        	       
	    }
              
	 private static void parseMSWord() throws IOException, SAXException, TikaException {
	    	
             ParseContext context = new ParseContext();
             Metadata metadata = new Metadata();
             WriteOutContentHandler handler = new WriteOutContentHandler(10 * 1024 * 1024);
             FileInputStream fis = new FileInputStream(file);
             OfficeParser officeParser = new OfficeParser();
             officeParser.parse(fis, handler, metadata, context);
             content = handler.toString();
	    }

    @SuppressWarnings("deprecation")
       private static void OCR(String fn) {
             
             File f = new File(fn);
             Tesseract instance =  new Tesseract(); 
             try {    
                     content = instance.doOCR(f);
                      //System.out.println(content);
             } catch (TesseractException e) {
                     System.err.println(e.getMessage());
             }
       }
	public static void setFOLDER_OF_PDFS(String fOLDER_OF_PDFS) {
		FOLDER_OF_PDFS = fOLDER_OF_PDFS;
	}

	public static String getFOLDER_OF_PDFS() {
		return FOLDER_OF_PDFS;
	}
	
	
	public  String[] getFileNames() {
		return fileNames;
	}
	
	public  String getContent() {
		return content;
	}
	
	public  String[] getContents() {
		return contents;
	}
}
