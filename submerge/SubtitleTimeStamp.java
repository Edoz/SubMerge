package submerge;

import java.util.Arrays;

public class SubtitleTimeStamp implements Comparable<SubtitleTimeStamp> {

	private static final String START_STOP_SEPARATOR = " --> ";
	private static final String HR_MIN_SEC_SEPARATOR = ":";
	private static final String MSEC_SEPARATOR = ",";

	private Integer[] startStopTimes; 
	// stores start and stop times in the following order
	// startHr, startMin, startSec, startMsec
	// stopHr, stopMin, stopSec, stopMsec
	
	/**
	 * creates timestamp object to hold start and end time for a subtitle line
	 * @param timeStampLine example time stamp line in SRT file is: 00:02:33,000 --> 00:02:32,000
	 * @throws NumberFormatException when the line is not properly formatted
	 */
	public SubtitleTimeStamp(String timeStampLine) throws NumberFormatException {
		startStopTimes = SubtitleTimeStamp.extractTimeStampFromLine(timeStampLine);
	}
	
	/**
	 * use this constructor to create offset time stamp, sets start time only, used only to add to other timestamps
	 * @param useShortLine dummy parameter to make sure we intend to call this constructor
	 * @param shortTimeStampLine has to be formatted correctly as hh:mm:ss
	 */
	public SubtitleTimeStamp(boolean useShortLine, String shortTimeStampLine) throws NumberFormatException {
		if(!useShortLine) throw new NumberFormatException(); // make sure we're using the intended constructor
		String timeStampLine = shortTimeStampLine + ",000" + SubtitleTimeStamp.START_STOP_SEPARATOR + "00:00:00,000";
		startStopTimes = SubtitleTimeStamp.extractTimeStampFromLine(timeStampLine);
	}
	
	public SubtitleTimeStamp(SubtitleTimeStamp timeStamp) {
		// constructs a deep copy of timeStamp
		startStopTimes = Arrays.copyOf(timeStamp.getStartStopTimes(), 8);
	}
	
	public SubtitleTimeStamp() {
		startStopTimes = new Integer[8];
		for(int i = 0; i < startStopTimes.length; i++) startStopTimes[i] = new Integer(0);
	}
	
	@Override
	public String toString() {
		// reconstructs and returns timestamp line
		String rtn = "", time;
		// start hr, min, sec
		for(int i = 0; i < 3; i++) {
			time = startStopTimes[i].toString();
			if(time.length() == 1) time = "0" + time;
			rtn = rtn + time + SubtitleTimeStamp.HR_MIN_SEC_SEPARATOR;
		}
		// start ms and start/stop separator
		time = startStopTimes[3].toString();
		rtn = rtn.substring(0, rtn.length()-1) + SubtitleTimeStamp.MSEC_SEPARATOR;
		if(time.length() == 1) time = "00" + time;
		else if(time.length() == 2) time = "0" + time;
		rtn = rtn + time + SubtitleTimeStamp.START_STOP_SEPARATOR;
		// stop hr, min, sec
		for(int i = 4; i < 7; i++) {
			time = startStopTimes[i].toString();
			if(time.length() == 1) time = "0" + time;
			rtn = rtn + time + SubtitleTimeStamp.HR_MIN_SEC_SEPARATOR;
		}
		// stop ms
		time = startStopTimes[7].toString();
		rtn = rtn.substring(0, rtn.length()-1) + SubtitleTimeStamp.MSEC_SEPARATOR;
		if(time.length() == 1) time = "00" + time;
		else if(time.length() == 2) time = "0" + time;
		rtn = rtn + time;
		
		return rtn;
	}

	protected static Integer[] extractTimeStampFromLine(String timeStampLine) throws NumberFormatException {
		
		// example time stamp is: 00:02:33,000 --> 00:02:32,000
		String[] startStopArray = timeStampLine.split(SubtitleTimeStamp.START_STOP_SEPARATOR);
		
		String[] startTimes = startStopArray[0].split(SubtitleTimeStamp.MSEC_SEPARATOR)[0].split(SubtitleTimeStamp.HR_MIN_SEC_SEPARATOR);
		String startMSec = startStopArray[0].split(SubtitleTimeStamp.MSEC_SEPARATOR)[1];
		
		String[] stopTimes = startStopArray[1].split(SubtitleTimeStamp.MSEC_SEPARATOR)[0].split(SubtitleTimeStamp.HR_MIN_SEC_SEPARATOR);
		String stopMSec = startStopArray[1].split(SubtitleTimeStamp.MSEC_SEPARATOR)[1];
		
		Integer[] values = new Integer[8];
		
		for(int i = 0; i < 3; i++) values[i] = Integer.parseInt(startTimes[i]);
		values[3] = Integer.parseInt(startMSec);
		for(int i = 4; i < 7; i++) values[i] = Integer.parseInt(stopTimes[i-4]);
		values[7] = Integer.parseInt(stopMSec);
		
		return values;
	}
	
	/**
	 * adjusts for msecs > 999, secs > 60, mins > 60
	 */
	public void adjust() {
		// msecs to secs
		this.set( TimeValues.STARTSEC, this.get(TimeValues.STARTSEC) + (this.get(TimeValues.STARTMSEC) / 1000) );
		this.set( TimeValues.STARTMSEC, this.get(TimeValues.STARTMSEC) % 1000 );
		// secs to min
		this.set( TimeValues.STARTMIN, this.get(TimeValues.STARTMIN) + (this.get(TimeValues.STARTSEC) / 60) );
		this.set( TimeValues.STARTSEC, this.get(TimeValues.STARTSEC) % 60 );
		// min to hrs
		this.set( TimeValues.STARTHR, this.get(TimeValues.STARTHR) + (this.get(TimeValues.STARTMIN) / 60) );
		this.set( TimeValues.STARTMIN, this.get(TimeValues.STARTMIN) % 60 );
		
		// same for stop times
		// msecs to secs
		this.set( TimeValues.STOPSEC, this.get(TimeValues.STOPSEC) + (this.get(TimeValues.STOPMSEC) / 1000) );
		this.set( TimeValues.STOPMSEC, this.get(TimeValues.STOPMSEC) % 1000 );
		// secs to min
		this.set( TimeValues.STOPMIN, this.get(TimeValues.STOPMIN) + (this.get(TimeValues.STOPSEC) / 60) );
		this.set( TimeValues.STOPSEC, this.get(TimeValues.STOPSEC) % 60 );
		// min to hrs
		this.set( TimeValues.STOPHR, this.get(TimeValues.STOPHR) + (this.get(TimeValues.STOPMIN) / 60) );
		this.set( TimeValues.STOPMIN, this.get(TimeValues.STOPMIN) % 60 );
	}
	
	/**
	 * 
	 * @param timeStamp a timeStamp to represent an offset. Only start times from timeStamp are used
	 * @return a SubtitleTimeStamp with both start and stop values offset by timeStamp's start values
	 * 
	 * e.g. ("00:02:33,000 --> 00:02:36,000").add("03:11:01,222 --> 99:99:99,999") would return ("03:14:09,222 --> 03:14:12,222")
	 */
	public SubtitleTimeStamp add(SubtitleTimeStamp timeStamp) {
		
		SubtitleTimeStamp newTimeStamp = new SubtitleTimeStamp(this);
		
		for(int i = 0; i < 8; i++) {
			if( i < 4 ) newTimeStamp.set(i, this.get(i) + timeStamp.get(i));
			else newTimeStamp.set(i, this.get(i) + timeStamp.get(i-4));
		}
		
		newTimeStamp.adjust();
		
		return newTimeStamp;
	}
	
	/**
	 * returns the difference in start times as a new subtitletimestamp
	 * @param newStartTime
	 * @return newStartTime - this
	 */
	public SubtitleTimeStamp getOffset(SubtitleTimeStamp newStartTime) {
		// create copies
		SubtitleTimeStamp offset = new SubtitleTimeStamp(this);
		SubtitleTimeStamp newStartTimeCopy = new SubtitleTimeStamp(newStartTime);
		// transfer everything to seconds to avoid negative values 
		//  so that 01:40:10 offset from 01:30:30 does not give 00:20:-20
		newStartTimeCopy.set(2, newStartTimeCopy.get(2) + newStartTimeCopy.get(1)*60 + newStartTimeCopy.get(0)*3600);
		newStartTimeCopy.set(1, 0);
		newStartTimeCopy.set(0, 0);
		offset.set(2, offset.get(2) + offset.get(1)*60 + offset.get(0)*3600);
		offset.set(1, 0);
		offset.set(0, 0);
		
		// subtract this from newstarttime
		offset.set(2, newStartTimeCopy.get(2) - offset.get(2));
		
		offset.adjust(); // put overflowing seconds back into minutes and hours
		return offset;
	}
	
	public Integer[] getStartStopTimes() {
		return startStopTimes;
	}

	public Integer get(TimeValues valueName) {
		return this.get(valueName.ordinal());
	}
	
	public Integer get(int index) {
		return startStopTimes[index];
	}
	
	void set(TimeValues valueName, Integer newValue) {
		this.set(valueName.ordinal(), newValue);
	}
	
	void set(int index, Integer newValue) {
		startStopTimes[index] = newValue;
	}

	@Override
	public int compareTo(SubtitleTimeStamp o) {
		// to use with a regular timestamp and one for starttime (containing only start hh:mm:ss)
		if(this.equals(o))
			return 0;
		else {
			for(int i = 0; i < 4; i ++) {
				if(this.get(i) < o.get(i))
					return -1;
				else if(this.get(i) > o.get(i))
					return 1;
			}
		}
		return 0; // if get here, means somehow didn't catch equal
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubtitleTimeStamp other = (SubtitleTimeStamp) obj;
		if (!Arrays.equals(startStopTimes, other.startStopTimes))
			return false;
		return true;
	}
}
