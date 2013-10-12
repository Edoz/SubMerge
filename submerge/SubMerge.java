package submerge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SubMerge {

	private SRTFileMgr fileMgr;
	private Scanner input;
	private ArrayList<String> inputFileNames;
	private ArrayList<SubtitleTimeStamp> offsets;
	
	//TODO: 
	//      get each line from fileMgr, process (add offset), send it to fileMgr for writing
	
	public static void main(String [] args) {
		SubMerge submerge = new SubMerge();
		submerge.input = new Scanner(System.in);
		submerge.inputFileNames = new ArrayList<String>();
		submerge.offsets = new ArrayList<SubtitleTimeStamp>();
		
		// get required user inputs and output file name
		String outputFileName = submerge.getAllUserInputs();
		// initialize srt file mgr
		try {
			submerge.fileMgr = new SRTFileMgr(submerge.inputFileNames, outputFileName);
		} catch (Exception e) {
			System.out.println("Could proceed: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		// now process files line by line and save them
		try {
			submerge.processFiles();
			submerge.fileMgr.saveAndCloseOutputFile();
		} catch (Exception e) {
			System.out.println("Could not process files: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Subtitles successfully merged / updated!");
	}
	
	/**
	 * reads input files, adds offsets, corrects numbering of lines
	 * @throws IOException 
	 */
	public void processFiles() throws IOException {
		int count = 0;
		// one cycle of this loops represents processing one single subtitle line group, such as:
		/*
		 * 1
		 * 00:02:16,000 --> 00:02:15,000
		 * here are one or more
		 * lines of text for this subtitle line, followed by an empty line, then number 2 and so on
		 * 
		 */
		while(true) {
			// get line indicating subtitle line number
			String countLine = fileMgr.getNextLine();
			// check if reached end of files
			if(countLine.equals("ENDOFSUBTITLEFILES")) break;
			
			count++;
			// disregard count in subtitle file since we may be appending files together. use our own count
			fileMgr.appendLine(new Integer(count).toString());
			
			// get timestamp and add offset
			String timeStampLine = fileMgr.getNextLine();
			SubtitleTimeStamp timeStamp = new SubtitleTimeStamp(timeStampLine);
			timeStamp = timeStamp.add(offsets.get(fileMgr.getCurrentIndex()));
			// substitute it into line
			timeStampLine = timeStamp.toString();
			// append to output
			fileMgr.appendLine(timeStampLine);
			
			// start loop to get all text lines of this subtitle line. empty line indicates end of text
			String nextLine = fileMgr.getNextLine();
			while(!nextLine.equals("") && !nextLine.equals("\n") && !nextLine.equals("\r\n")) {
				fileMgr.appendLine(nextLine);
				nextLine = fileMgr.getNextLine();
			}
			
			// append empty line after subtitle line group
			fileMgr.appendCRLF();
		}
	}
	
	/**
	 * goes through various loops to ask and validate for all input files, their subtitle offsets if any, and the output file name
	 * @return a string containing the validated output file name (created) to pass to srt file mgr
	 */
	public String getAllUserInputs() {
		String fileName;
		SubtitleTimeStamp offset;
		
		while(true) {
			while(true) {
				try {
					fileName = askInputFileName();
					// check if null
					if(fileName == null) throw new NullPointerException();
					break;
				}
				catch (NullPointerException e) {
					System.out.println("Could not open file. Make sure file is in same directory as SubMerge");
					continue;
				}
			} // end ask for filename loop
			while(true) {
				try {
					offset = getNewStartTime(fileName);
					if(!offset.equals(new SubtitleTimeStamp("00:00:00,000 --> 00:00:00,000"))) {
						// check if new start time (still in offset) > actual start time (unless it's 0 => no change)
						if( offset.compareTo(new SubtitleTimeStamp(SRTFileMgr.getFirstTimeStampLine(fileName))) == -1) {
							System.out.println("New start time must be greater than current file's start time. Enter again.");
							continue;
						}
						// set offset to actual offset
						offset = new SubtitleTimeStamp(SRTFileMgr.getFirstTimeStampLine(fileName)).getOffset(offset);
					}
					break;
				} catch (FileNotFoundException e) {
					continue;
				} catch (NumberFormatException e) {
					continue;
				}
			} // end ask for offset loop
			// everything went ok, add input and offset to our list
			inputFileNames.add(new String(fileName));
			offsets.add(new SubtitleTimeStamp(offset));
			// ask if we want to add another file, if not, ask for output filename, then break
			if(!askIfWantAddNewFile()) {
				while(true) {
					try {
						fileName = askOutputFileName();
						if(fileName == null) throw new IOException();
						break;
					} catch (IOException e) {
						System.out.println("Could not create file. Enter new name.");
						continue;
					}
				}
				break;
			}
		} // end ask for input/offsets loop, filename now contains output file name
		return fileName;
	}
	
	/**
	 * 
	 * @return null if no file was found, otherwise the filename
	 */
	public String askInputFileName() {
		System.out.print("Enter subtitle filename: ");
		String fileName = input.nextLine();
		// check if file exists
		File f = new File(fileName);
		if(f != null && f.isFile() && f.canRead())
			return fileName;
		else
			return null;
	}
	
	/**
	 * 
	 * @return null if no file was found, otherwise the filename
	 * @throws IOException 
	 */
	public String askOutputFileName() throws IOException {
		System.out.print("Enter name for output file (include .srt extension): ");
		String fileName = input.nextLine();
		// check if file exists
		File f = new File(fileName);
		if(f != null && f.createNewFile() && f.canWrite())
			return fileName;
		else
			return null;
	}
	
	/**
	 * asks for new start time and returns it as a SubtitleTimeStamp 
	 * @return
	 * @throws FileNotFoundException 
	 */
	public SubtitleTimeStamp getNewStartTime(String correspondingFileName) throws FileNotFoundException {
		// get startTime to display in request
		String stTime = SRTFileMgr.getFirstTimeStampLine(correspondingFileName);
		System.out.println("First subtitle line start time for file entered is " + stTime.substring(0, 8));
		System.out.print("Enter new start time for first subtitle line for file entered (has to be greater than above), format hh:mm:ss, or 0 to leave it unchanged: ");
		String startTimeString = input.nextLine();
		// try to parse
		if(startTimeString.equals("0")) {
			return new SubtitleTimeStamp(); // default constructor set all times to 0
		} else {
			return new SubtitleTimeStamp(true, startTimeString);
		}
	}
	
	/**
	 * asks if user wants to add a new file until we get a valid answer
	 * @return true if user wants to add a file, false if he doesn't
	 */
	public boolean askIfWantAddNewFile() {
		while(true) {
			System.out.print("Add another file? y/n: ");
			String answer = input.nextLine();
			if(answer == null) continue;
			else if(answer.equalsIgnoreCase("y")) return true;
			else if (answer.equalsIgnoreCase("n")) return false;
		}
	}
}
