import java.math.BigInteger;
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

    static class Segmentator implements Iterable<byte[]>, Iterator<byte[]> {
        public Segmentator(byte[] bytes, int segBitLength) {
            this.bytes = bytes;
            this.segBitLength = segBitLength;
            this.segByteNum = segBitLength / 8;
            this.segExtraBits = segBitLength - segByteNum * 8;
            this.i = 0;
            this.shift = 0;
        }

        @Override
        public Iterator<byte[]> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return i < this.bytes.length;
        }

        @Override
        public byte[] next() {
            byte[] segment = new byte[segByteNum + 1];
            int current = this.bytes[this.i] & 0xFF;
            for(int i = 0; i < segByteNum; i++) {
                if(this.i == this.bytes.length - 1) {
                    segment[i] = (byte)(current << (24+shift) >>> 24);
                    for(int k = i+1; k < segByteNum+1; k++)
                        segment[k] = 0;
                    this.i++;
                    return segment;
                }
                else {
                    int next = this.bytes[this.i+1] & 0xFF;
                    segment[i] = (byte)(current << (24+shift) >>> 24 | next >>> (8-shift));
                    this.i++;
                }
                current = this.bytes[this.i] & 0xFF;
            }
            segment[segByteNum] = (byte)(current >>> (8 - this.shift) << (8 - this.shift));
            this.shift = (this.shift + segExtraBits) % 8;
            return segment;
        }

        private final byte[] bytes; //source of data
        private final int segBitLength; //number of bits in a segment
        private final int segByteNum; //number of whole bytes in a segment
        private final int segExtraBits; //number of bits of the last byte in a segment
        private int i; //index
        private int shift;
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
            return i+2 < this.bytes.length;
        }

        @Override
        public Byte next() {
            byte result = 0;
            int available = 8;
            while(available != 0) {
                int current = bytes[i] & 0xFF;
                if((i+1) % (segByteNum+1) == 0) {
                    available -= segExtraBits;
                    result |= current >>> (8-segExtraBits) << available;
                    if(this.hasNext()) result |= (bytes[i+1]&0xFF) >>> (8-available);
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
                result &= 0xFF;
                this.i++;
            }

            return result;
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
