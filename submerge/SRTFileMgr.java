package submerge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

class SRTFileMgr {

	ArrayList<Scanner> readers;
	Writer writer;
	
	String prevLine;
	int currentIndex = 0;
		
	public int getCurrentIndex() { return currentIndex; }
	
	/**
	 * 
	 * @param inputFiles arrayList of strings containing the filenames of all the input srt files
	 * @param outputFileName the name of output file containing the merged inputfiles
	 * @throws FileNotFoundException when input files are not found or provided
	 * @throws UnsupportedEncodingException when it can't create the output file stream in us-ascii
	 */
	public SRTFileMgr(ArrayList<String> inputFiles, String outputFileName) throws FileNotFoundException, UnsupportedEncodingException {
		
		if( inputFiles == null || inputFiles.size() == 0 )
			throw new FileNotFoundException("Need at least one input file");
		
		readers = new ArrayList<Scanner>(inputFiles.size());

		for(String file : inputFiles)
			readers.add(new Scanner(new File(file)));
		
		prevLine = "";
		writer = new OutputStreamWriter(new FileOutputStream(outputFileName), "us-ascii");
	}
	
	/**
	 * helper method to return the first timestamp line for display
	 * @param filename
	 * @return the first timestamp line
	 */
	public static String getFirstTimeStampLine(String filename) throws FileNotFoundException {
		Scanner reader = new Scanner(new File(filename));
		reader.nextLine();
		return reader.nextLine();
	}
	
	/**
	 * makes sure we don't return two LF or CRLF in a row
	 * @return the next line in the concatenated files, or "ENDOFSUBTITLEFILES" if exhausted all files
	 */
	public String getNextLine() {
		
		if(currentIndex >= readers.size()) return "ENDOFSUBTITLEFILES";
		
		while(!readers.get(currentIndex).hasNextLine()) {
			
			if(currentIndex >= readers.size()) return "ENDOFSUBTITLEFILES";
			
			else {
				currentIndex++;
				if(prevLine == "\n" || prevLine == "\r\n")
				{
						return "";
				}
				else {
					prevLine = "\n";
					return "\n";
				}
			}
		}

		prevLine = readers.get(currentIndex).nextLine();
		return prevLine;
	}
	
	/**
	 * appenda the given string to the output as a new line, padding with \r\n if necessary (CR LF)
	 * @param line
	 * @throws IOException
	 */
	public void appendLine(String line) throws IOException {
		line = line.replace("\n", "");
		line = line.replace("\r", "");
		// in case it did it twice by accident
		line = line.replace("\r\r\n", "");
		
		char[] lineChars = line.toCharArray();
		for(char c : lineChars) {
			writer.append(c);
		}
		appendCRLF();
	}
	
	public void appendCRLF() throws IOException {
		writer.append((char)13);
		writer.append((char)10);
	}
	
	public void saveAndCloseOutputFile() throws IOException {
		writer.flush();
		writer.close();
	}
}
