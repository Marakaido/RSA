import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SegmentationTest {
    byte[] bytes = new byte[]{
            (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101
    };

    @Test public void segmentationFirstSegmentTest() {
        RSA.Segmentator segmentator = new RSA.Segmentator(bytes, 9);
        assertArrayEquals(new byte[]{(byte)0b0, (byte)0b11011100}, segmentator.next());
    }

    @Test public void segmentationMiddleSegmentTest() {
        RSA.Segmentator segmentator = new RSA.Segmentator(bytes, 9);
        segmentator.next();
        assertArrayEquals(new byte[]{(byte)0b1, (byte)0b00010111}, segmentator.next());
    }

    @Test public void lastSegmentSegmentationTest() {
        RSA.Segmentator segmentator = new RSA.Segmentator(bytes, 10);
        segmentator.next();
        segmentator.next();
        segmentator.next();
        assertArrayEquals(new byte[]{(byte)1}, segmentator.next());
    }

    @Test public void firstSegmentDesegmentationTest() {
        byte[] bytes = new byte[] {
                (byte)0b0, (byte)0b11011100, (byte)0b1, (byte)0b00010111, (byte)0b0, (byte)0b11000101, (byte)0b10101
        };
        RSA.Desegmentator desegmentator = new RSA.Desegmentator(bytes, 9);
        assertEquals(0b01101110, desegmentator.next() & 0xFF);
    }

    @Test public void middleSegmentDesegmentationTest() {
        byte[] bytes = new byte[] {
                (byte)0b0, (byte)0b11011100, (byte)0b1, (byte)0b00010111, (byte)0b0, (byte)0b11000101, (byte)0b10101
        };
        RSA.Desegmentator desegmentator = new RSA.Desegmentator(bytes, 9);
        desegmentator.next();
        assertEquals(0b01000101, desegmentator.next() & 0xFF);
        assertEquals(0b11011000, desegmentator.next() & 0xFF);
    }

    @Test public void lastSegmentDesegmentationTest() {
        byte[] bytes = new byte[] {
                (byte)0b0, (byte)0b11011100, (byte)0b1, (byte)0b00010111, (byte)0b0, (byte)0b11000101, (byte)0b10101
        };
        RSA.Desegmentator desegmentator = new RSA.Desegmentator(bytes, 9);
        desegmentator.next();
        desegmentator.next();
        desegmentator.next();
        assertEquals(0b10110101, desegmentator.next() & 0xFF);
    }


}
