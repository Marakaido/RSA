import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

public class RSA {
    public RSA(BigInteger p, BigInteger q) {
        this.n = p.multiply(q);
        BigInteger fi_n = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        this.e = calculateE(fi_n);
        this.d = calculateD(fi_n, e);
        this.segBitNum = (int)(Math.log(n.doubleValue()) / Math.log(2));
        this.segByteNum = segBitNum / 8 + 1;
    }

    public BigInteger getN() { return this.n; }
    public BigInteger getE() { return this.e; }

    public static void encrypt(InputStream in, OutputStream out, BigInteger e, BigInteger n) throws IOException {
        int segBitNum = (int)(Math.log(n.doubleValue()) / Math.log(2));
        int segByteNum = segBitNum / 8 + 1;
        byte[] bytes = IOUtils.toByteArray(in);
        for (int i = 0; i < bytes.length;) {
            byte[] segment = new byte[segByteNum];
            segment[0] = 0;
            for (int j = 1; j < segByteNum; j++)
                segment[j] = bytes[i++];

            byte[] encrypted = new BigInteger(segment).modPow(e, n).toByteArray();
            if(encrypted.length < segByteNum)
                for (int j = segByteNum-1; j >= 0; j--) {
                    if(j < segByteNum - encrypted.length) segment[j] = 0;
                    else segment[j] = encrypted[j-segByteNum+encrypted.length];
                }
            else segment = encrypted;
            out.write(segment);
        }

    }

    public void decrypt(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = IOUtils.toByteArray(in);
        for (int i = 0; i < bytes.length;) {
            byte[] segment = new byte[segByteNum];
            for (int j = 0; j < segByteNum; j++)
                segment[j] = bytes[i++];
            byte[] decrypted = new BigInteger(segment).modPow(d, n).toByteArray();
            if(decrypted.length != segByteNum-1)
                decrypted = Arrays.copyOfRange(decrypted, 1, decrypted.length);
            out.write(decrypted);
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

    private BigInteger n;
    private BigInteger e;
    private BigInteger d;
    private final int segByteNum;
    private final int segBitNum;
}
