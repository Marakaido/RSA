import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BitIteratorTest {
    byte[] bytes = new byte[]{
            (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101
    };

    @Test public void bitIterator8WholeBitsTest() {
        int value = 0b10101111;
        RSA.BitIterator bitIterator = new RSA.BitIterator(new byte[] {(byte)value});
        assertEquals(value, bitIterator.next(8));
    }

    @Test public void bitIteratorPartialBitsTest() {
        int value = 0b10101111;
        RSA.BitIterator bitIterator = new RSA.BitIterator(new byte[] {(byte)value});
        assertEquals(0b101011, bitIterator.next(6));
    }

    @Test public void bitIteratorSeparatedBitsTest() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(Arrays.copyOf(bytes, bytes.length));
        bitIterator.next(6);
        assertEquals(0b10010, bitIterator.next(5));
    }

    @Test public void bitIteratorSeparatedBitsTest2() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(Arrays.copyOf(bytes, bytes.length));
        bitIterator.next(1);
        assertEquals(0b11011100, bitIterator.next(8));
    }

    @Test public void longTest2() {
        byte[] bytes = new byte[] {
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101,
                (byte)0b01101110,(byte)0b01000101, (byte)0b11011000, (byte)0b10110101
        };
        int[] expected = new int[] {
                0b01, 0b10111001,
                0b00, 0b01011101,
                0b10, 0b00101101,
                0b01, 0b01101110,
                0b01, 0b00010111,
                0b01, 0b10001011,
                0b0101
        };
        RSA.BitIterator bitIterator = new RSA.BitIterator(bytes);
        for (int i = 0; i < expected.length - 1; i++) {
            if(i%2==0) assertEquals(expected[i], bitIterator.next(2));
            else assertEquals(expected[i], bitIterator.next(8) & 0xFF);
        }
        assertEquals(expected[expected.length-1], bitIterator.next(4));
    }

    @Test public void bitIteratorNotEnoughBitsTest() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(new byte[]{(byte) 0b11111111});
        bitIterator.next(6);
        assertEquals(0b11, bitIterator.next(8));
    }

    @Test public void longTest() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(bytes);
        bitIterator.next(1);
        bitIterator.next(8);

        assertEquals(0b1, bitIterator.next(1));
    }
}
