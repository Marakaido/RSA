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

    @Test public void bitIteratorNotEnoughBitsTest() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(new byte[]{(byte) 0b11111111});
        bitIterator.next(6);
        assertEquals(0b11000, bitIterator.next(5));
    }

    @Test public void longTest() {
        RSA.BitIterator bitIterator = new RSA.BitIterator(bytes);
        bitIterator.next(1);
        bitIterator.next(8);

        assertEquals(0b1, bitIterator.next(1));
    }
}
