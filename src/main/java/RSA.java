import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

public class RSA {
    public RSA(BigInteger p, BigInteger q) {
        this.n = p.multiply(q);
        BigInteger fi_n = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        this.e = calculateE(fi_n);
        this.d = calculateD(fi_n, e);
    }

    public BigInteger getN() { return this.n; }
    public BigInteger getE() { return this.e; }


    public static void encrypt(InputStream in, OutputStream out, BigInteger e, BigInteger n) throws IOException {
        byte[] bytes = IOUtils.toByteArray(in);
        int segByteNum = getSegBitNumber(n) / 8 + 1;
        Segmentator segmentator = new Segmentator(bytes, getSegBitNumber(n));
        for(byte[] b : segmentator) {
            byte[] encrypted = new byte[segByteNum];
            byte[] arr = new BigInteger(b).modPow(e, n).toByteArray();
            for (int i = encrypted.length - 1, j = arr.length - 1; j >= 0; i--, j--)
                encrypted[i] = arr[j];
            out.write(encrypted);
        }
    }

    public void decrypt(InputStream in, OutputStream out) throws IOException {
        int segByteNum = getSegBitNumber(n) / 8 + 1;
        ByteArrayOutputStream segmented = new ByteArrayOutputStream();
        while(in.available() != 0) {
            byte[] segment = new byte[segByteNum];
            byte[] decrypted = new byte[segByteNum];
            in.read(segment);
            byte[] arr = new BigInteger(segment).modPow(d, n).toByteArray();
            for (int i = decrypted.length - 1, j = arr.length - 1; j >= 0; i--, j--)
                decrypted[i] = arr[j];
            segmented.write(decrypted);
        }
        Desegmentator d = new Desegmentator(segmented.toByteArray(), getSegBitNumber(n));
        for(byte b : d) {
            out.write(b);
        }
    }

    private static int getSegBitNumber(BigInteger n) {
        return (int)(Math.log(n.doubleValue()) / Math.log(2)) + 1;
    }

    private BigInteger generateKeys(BigInteger p, BigInteger q) {
        BigInteger lambda = p.multiply(q).divide(p.gcd(q));
        return lambda;
    }

    private BigInteger calculateE(BigInteger fi_n)
    {
        BigInteger e = BigInteger.valueOf(2);
        while(!e.equals(fi_n))
        {
            if(fi_n.gcd(e).equals(BigInteger.ONE))
                return e;
            e = e.add(BigInteger.ONE);
        }
        return e;
    }

    private BigInteger calculateD(BigInteger fi_n, BigInteger e)
    {
        BigInteger d = fi_n.subtract(BigInteger.ONE);
        while(!d.equals(e))
        {
            if(!e.multiply(d).mod(fi_n).equals(BigInteger.ONE))
                d = d.subtract(BigInteger.ONE);
            else break;
        }
        return d;
    }

    static class BitIterator {
        public BitIterator(byte[] bytes) {
            this.bytes = bytes;
        }

        public boolean hasNext() {
            return i < bytes.length;
        }

        public int next(int n) {
            if(!hasNext()) throw new IllegalStateException("No more bits");
            int result = 0;
            int current = bytes[i] & 0xFF;
            if(isLast() && n > available) n = available;
            if(n <= available) {
                result = current << (32 - available) >>> (32 - n);
                available -= n;
            }
            else {
                result = current << (32 - available) >>> (32 - n);
                int left = n - available;
                if(++i < bytes.length)
                    result |= (bytes[i] & 0xFF) >>> (8-left);
                available = 8 - left;
            }
            if(available == 0) {
                i++;
                available = 8;
            }
            return result;
        }

        private boolean isLast() {
            return i == bytes.length-1;
        }

        private int i = 0;
        private int available = 8;
        private byte[] bytes;
    }

    static class Segmentator implements Iterable<byte[]>, Iterator<byte[]> {
        public Segmentator(byte[] bytes, int segBitLength) {
            this.bytes = bytes;
            this.segBitLength = segBitLength;
            this.segByteNum = segBitLength / 8;
            this.segExtraBits = segBitLength - segByteNum * 8;
            this.segNum = bytes.length * 8 / segBitLength;
            this.i = 0;
            this.shift = 0;
            this.bitIterator = new BitIterator(this.bytes);
        }

        @Override
        public Iterator<byte[]> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return bitIterator.hasNext();
        }

        @Override
        public byte[] next() {
            byte[] segment = null;
            if(!isLast()) {
                segment = new byte[segByteNum + 1];
                segment[0] = (byte)bitIterator.next(segExtraBits);
                int j = 1;
                shift = (shift + segExtraBits) % 8;
                for (; j < segByteNum+1; j++)
                    if(bitIterator.hasNext()) segment[j] = (byte) bitIterator.next(8);
                    else break;
            }
            else {
                segment = new byte[segByteNum];
                for (int j = 0; j < segByteNum; j++)
                    if(bitIterator.hasNext()) segment[j] = (byte) bitIterator.next(8);
                    else break;
            }

            i++;
            return segment;
        }

        private boolean isLast() {
            return i == segNum;
        }

        private final byte[] bytes; //source of data
        private final int segBitLength; //number of bits in a segment
        private final int segByteNum; //number of whole bytes in a segment
        private final int segExtraBits; //number of bits of the last byte in a segment
        private int i; //index
        private int shift;
        private final int segNum;
        private final BitIterator bitIterator;
    }

    static class Desegmentator implements Iterable<Byte>, Iterator<Byte> {
        public Desegmentator(byte[] bytes, int segBitLength) {
            this.bytes = bytes;
            this.segBitLength = segBitLength;
            this.segByteNum = segBitLength / 8;
            this.segExtraBits = segBitLength - segByteNum * 8;
            this.i = 0;
            this.bitsTaken = 0;
            this.shift = 0;
        }

        @Override
        public Iterator<Byte> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return i+1 < this.bytes.length;
        }

        @Override
        public Byte next() {
            int result = 0;
            int available = 8;
            while(available != 0) {
                int current = bytes[i] & 0xFF;

                if(i % (segByteNum+1) == 0) {
                    result |= current << (available - segExtraBits);
                    available -= segExtraBits;
                    result |= (bytes[i+1]&0xFF) >>> (8-available);
                    this.bitsTaken = available;
                    available = 0;
                }
                else {
                    int offeredBits = current << (24+bitsTaken) >>> (24+bitsTaken);
                    int offeredNum = 8 - bitsTaken;
                    if(offeredNum >= available) {
                        this.bitsTaken = offeredNum - available;
                        result |= offeredBits >>> this.bitsTaken;
                        available = 0;
                    }
                    else if(offeredNum < available) {
                        this.bitsTaken = 0;
                        available -= offeredNum;
                        result |= offeredBits << available;
                    }
                }

                this.i++;
                if(!hasNext()) return (byte)(result | bytes[i] << (24 + bitsTaken) >>> (24 + bitsTaken));
            }

            return (byte)result;
        }

        private final byte[] bytes; //source of data
        private final int segBitLength; //number of bits in a segment
        private final int segByteNum; //number of whole bytes in a segment
        private final int segExtraBits; //number of bits of the last byte in a segment
        private int i; //index
        private int bitsTaken;
        private int shift;
    }


    private BigInteger n;
    private BigInteger e;
    private BigInteger d;
}
