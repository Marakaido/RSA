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

    @Test public void longSegmentationTest() {
        byte[] bytes = new byte[] {
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101,
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101
        };
        byte[][] expected = new byte[][] {
                new byte[]{(byte)0b01, (byte)0b10111001},
                new byte[]{0b00, 0b01011101},
                new byte[]{0b10, 0b00101101},
                new byte[]{0b01, (byte)0b01101110},
                new byte[]{(byte)0b01, 0b00010111},
                new byte[]{0b01, (byte)0b10001011},
                new byte[]{0b0101}
        };
        RSA.Segmentator segmentator = new RSA.Segmentator(bytes, 10);
        int i = 0;
        for(byte[] b : segmentator)
            assertArrayEquals(expected[i++], b);
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

    @Test public void longDesegmentationTest() {
        byte[] bytes = new byte[] {
                0b01, (byte)0b10111001,
                0b00, 0b01011101,
                0b10, 0b00101101,
                0b01, 0b01101110,
                0b01, 0b00010111,
                0b01, (byte)0b10001011,
                0b0101
        };
        byte[] expected = new byte[] {
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101,
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101
        };
        RSA.Desegmentator desegmentator = new RSA.Desegmentator(bytes, 10);
        int i = 0;
        for(byte b : desegmentator)
            assertEquals(expected[i++]&0xFF, b&0xFF);
    }


}
