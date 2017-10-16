import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Application {
    public static void main(String[] args) {
        try {
            CommandLine cmd = initCLI(args);
            if(cmd.hasOption("p") && cmd.hasOption("q")) {
                RSA rsa = new RSA(new BigInteger(cmd.getOptionValue("p")), new BigInteger(cmd.getOptionValue("q")));
                if(cmd.hasOption("show_key"))
                    System.out.println("Public key: \ne=" + rsa.getE() + "\nn=" + rsa.getN());

                if(cmd.hasOption("i") && cmd.hasOption("o")) {
                    InputStream in = new BufferedInputStream(new FileInputStream(Paths.get(cmd.getOptionValue("i")).toFile()));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(Paths.get(cmd.getOptionValue("o")).toFile()));
                    if(cmd.hasOption("h")) {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(IOUtils.toByteArray(in));
                    }
                    rsa.decrypt(in, out);
                }
            }
            else if(cmd.hasOption("e") && cmd.hasOption("n")) {
                BigInteger e = new BigInteger(cmd.getOptionValue("e"));
                BigInteger n = new BigInteger(cmd.getOptionValue("n"));
                if(cmd.hasOption("i") && cmd.hasOption("o")) {
                    InputStream in = new BufferedInputStream(new FileInputStream(Paths.get(cmd.getOptionValue("i")).toFile()));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(Paths.get(cmd.getOptionValue("o")).toFile()));
                    if(cmd.hasOption("h")) {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(IOUtils.toByteArray(in));
                    }
                    RSA.encrypt(in, out, e, n);
                }
            }
        }
        catch (NoSuchAlgorithmException e) {

        }
        catch(ParseException e) {
            System.err.println(e.getMessage());
        }
        catch(FileNotFoundException e) {

        }
        catch (IOException e) {

        }
    }

    public static CommandLine initCLI(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", false, "use hashing");
        options.addOption("e", true, "set e");
        options.addOption("n", true, "set n");
        options.addOption("p", true, "set p");
        options.addOption("q", true, "set q");
        options.addOption("show_key", false, "show key");
        options.addOption("i", true, "input file path");
        options.addOption("o", true, "output file path");
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
}
