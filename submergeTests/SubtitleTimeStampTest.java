package submergeTests;

import submerge.SubtitleTimeStamp;
import submerge.TimeValues;
import junit.framework.*;

public class SubtitleTimeStampTest extends TestCase {

	public void testConstructors() {
		
		SubtitleTimeStamp p = new SubtitleTimeStamp("00:02:33,000 --> 51:40:00,312");
		
		assertEquals((Integer)0, p.get(TimeValues.STARTHR));
		assertEquals((Integer)2, p.get(TimeValues.STARTMIN));
		assertEquals((Integer)33, p.get(TimeValues.STARTSEC));
		assertEquals((Integer)0, p.get(TimeValues.STARTMSEC));
		assertEquals((Integer)51, p.get(TimeValues.STOPHR));
		assertEquals((Integer)40, p.get(TimeValues.STOPMIN));
		assertEquals((Integer)0, p.get(TimeValues.STOPSEC));
		assertEquals((Integer)312, p.get(TimeValues.STOPMSEC));
		
		p = new SubtitleTimeStamp(p);
		
		assertEquals((Integer)0, p.get(TimeValues.STARTHR));
		assertEquals((Integer)2, p.get(TimeValues.STARTMIN));
		assertEquals((Integer)33, p.get(TimeValues.STARTSEC));
		assertEquals((Integer)0, p.get(TimeValues.STARTMSEC));
		assertEquals((Integer)51, p.get(TimeValues.STOPHR));
		assertEquals((Integer)40, p.get(TimeValues.STOPMIN));
		assertEquals((Integer)0, p.get(TimeValues.STOPSEC));
		assertEquals((Integer)312, p.get(TimeValues.STOPMSEC));
	}
	
	public void testAdd() {
		
		SubtitleTimeStamp p,c;
		p = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		p = p.add(p);
		
		assertEquals((Integer)10, p.get(TimeValues.STARTHR));
		assertEquals((Integer)20, p.get(TimeValues.STARTMIN));
		assertEquals((Integer)30, p.get(TimeValues.STARTSEC));
		assertEquals((Integer)800, p.get(TimeValues.STARTMSEC));
		assertEquals((Integer)56, p.get(TimeValues.STOPHR));
		assertEquals((Integer)0, p.get(TimeValues.STOPMIN));
		assertEquals((Integer)0, p.get(TimeValues.STOPSEC));
		assertEquals((Integer)0, p.get(TimeValues.STOPMSEC));
		
		p = new SubtitleTimeStamp("05:10:15,400 --> 50:59:54,812");
		p = p.add(p);
		
		assertEquals((Integer)10, p.get(TimeValues.STARTHR));
		assertEquals((Integer)20, p.get(TimeValues.STARTMIN));
		assertEquals((Integer)30, p.get(TimeValues.STARTSEC));
		assertEquals((Integer)800, p.get(TimeValues.STARTMSEC));
		assertEquals((Integer)56, p.get(TimeValues.STOPHR));
		assertEquals((Integer)10, p.get(TimeValues.STOPMIN));
		assertEquals((Integer)10, p.get(TimeValues.STOPSEC));
		assertEquals((Integer)212, p.get(TimeValues.STOPMSEC));
		
		p = new SubtitleTimeStamp("00:00:01,000 --> 00:00:04,074");
		c = new SubtitleTimeStamp("00:00:00,000 --> 00:00:00,000");
		
		assertEquals((Integer)0, p.get(TimeValues.STARTHR));
		assertEquals((Integer)0, p.get(TimeValues.STARTMIN));
		assertEquals((Integer)1, p.get(TimeValues.STARTSEC));
		assertEquals((Integer)0, p.get(TimeValues.STARTMSEC));
		assertEquals((Integer)0, p.get(TimeValues.STOPHR));
		assertEquals((Integer)0, p.get(TimeValues.STOPMIN));
		assertEquals((Integer)4, p.get(TimeValues.STOPSEC));
		assertEquals((Integer)74, p.get(TimeValues.STOPMSEC));
	}
	
	public void testGetOffset() {
		SubtitleTimeStamp a, b;
		
		a = new SubtitleTimeStamp("05:10:15,000 --> 00:00:00,000");
		b = new SubtitleTimeStamp("05:10:15,000 --> 00:00:00,000");
		assertEquals(new SubtitleTimeStamp().toString(), a.getOffset(b).toString());
		
		a = new SubtitleTimeStamp("05:10:15,400 --> 00:00:00,000");
		b = new SubtitleTimeStamp("05:11:00,100 --> 00:00:00,000");
		assertEquals("00:00:45,400 --> 00:00:00,000", a.getOffset(b).toString());
		
		a = new SubtitleTimeStamp("10:10:01,000 --> 00:00:00,000");
		b = new SubtitleTimeStamp("10:20:00,000 --> 00:00:00,000");
		assertEquals("00:09:59,000 --> 00:00:00,000", a.getOffset(b).toString());

	}
	
	public void testToString() {
		SubtitleTimeStamp p;
		
		p = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		assertEquals("05:10:15,400 --> 50:49:44,600", p.toString());
		
		p = new SubtitleTimeStamp("05:10:15,400 --> 50:59:54,812");
		assertEquals("05:10:15,400 --> 50:59:54,812", p.toString());
		
		p = new SubtitleTimeStamp("00:00:00,000 --> 00:00:00,000");
		assertEquals("00:00:00,000 --> 00:00:00,000", p.toString());
	}
	
	public void testCompareTo() {
		SubtitleTimeStamp a, b;
		
		a = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		b = a;
		assertEquals(0, a.compareTo(b));
		b = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		assertEquals(0, a.compareTo(b));
		assertEquals(0, b.compareTo(a));
		
		a = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		b = new SubtitleTimeStamp(true, "04:00:00");
		assertEquals(1, a.compareTo(b));
		assertEquals(-1, b.compareTo(a));
		
		a = new SubtitleTimeStamp("00:02:16,000 --> 00:02:15,000");
		b = new SubtitleTimeStamp(true, "00:11:00");
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
		
		a = new SubtitleTimeStamp("05:10:15,400 --> 50:49:44,600");
		b = new SubtitleTimeStamp(true, "05:10:15,500 --> 50:49:44,600");
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}
}
