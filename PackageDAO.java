import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class PackageDAO {

    public final static String HEADER_DELIM = "-----";
    public final static String PACKAGE_START = HEADER_DELIM + "BEGIN PACKAGE" + HEADER_DELIM;
    public final static String PACKAGE_END = HEADER_DELIM + "END PACKAGE" + HEADER_DELIM;
    public final static String BLOCK_START = HEADER_DELIM + "BEGIN BLOCK";
    public final static String BLOCK_END = HEADER_DELIM + "END BLOCK";
    public final static String BLOCK_START_TEMPLATE = BLOCK_START + " %s" + HEADER_DELIM;
    public final static String BLOCK_END_TEMPLATE = BLOCK_END + " %s" + HEADER_DELIM;
    public final static int LINE_WIDTH = 65;

    public static Package readPackage(String fileName) {
        Package result = null;
        try {
            InputStream in = new FileInputStream(fileName);
            result = readPackage(in);
            in.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Package file does not exist " + fileName);
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error in package file " + fileName);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        return result;
    }

    public static void writePackage(String fileName, Package pack) {
        try {
            PrintStream out = new PrintStream(fileName);
            writePackage(out, pack);
            out.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error writing package file " + fileName);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static Package readPackage(InputStream input) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        String line = in.readLine();

        Package result = new Package();

        while (!line.equals(PACKAGE_START)) {
            line = in.readLine();
        }
        Block block = readBlock(in);
        while (block != null) {
            result.addBlock(block.getName(), block.getContent());
            block = readBlock(in);
        }

        return result;

    }

    private static void writePackage(PrintStream out, Package pack) {
        out.println(PACKAGE_START);
        for (String blockName : pack.getBlockNames()) {
            writeBlock(out, blockName, pack.getContentBlock(blockName));
        }
        out.println(PACKAGE_END);
    }

    private static void writeBlock(PrintStream out, String blockName, byte[] content) {
        if ((blockName != null) && (content != null)) {
            out.printf(BLOCK_START_TEMPLATE + "\n", blockName);

            byte[] contentBase64 = Base64.getEncoder().encode(content);

            int lines = contentBase64.length / LINE_WIDTH;
            int rest = contentBase64.length % LINE_WIDTH;
            for (int i = 0; i < lines; i++) {
                out.println(new String(contentBase64, i * LINE_WIDTH, LINE_WIDTH));
            }
            out.println(new String(contentBase64, lines * LINE_WIDTH, rest));

            out.printf(BLOCK_END_TEMPLATE + "\n", blockName);
        }
    }

    private static Block readBlock(BufferedReader in) throws IOException {

        String line = in.readLine();
        while ((!line.startsWith(BLOCK_START) && (!line.equals(PACKAGE_END)))) {
            line = in.readLine();
        }
        if (line.equals(PACKAGE_END)) {
            return null;
        } else {
            Block result = new Block();
            result.setName(extractNameBlock(line));
            result.setContent(extractContentBlock(in));
            return result;
        }
    }

    private static String extractNameBlock(String text) {
        int startBlockName = BLOCK_START.length() + 1;
        int endBlockName = text.lastIndexOf(HEADER_DELIM);
        return text.substring(startBlockName, endBlockName);
    }

    private static byte[] extractContentBlock(BufferedReader in) throws IOException {
        List<String> blockParts = new ArrayList<String>();
        int blockSize = 0;

        String line = in.readLine();
        while (!line.startsWith(BLOCK_END)) {
            blockParts.add(line);
            blockSize += line.length();
            line = in.readLine();
        }

        byte[] result = new byte[blockSize];
        int position = 0;
        for (String part : blockParts) {
            byte[] partContent = part.getBytes();
            for (byte b : partContent) {
                result[position] = b;
                position++;
            }
        }
        return Base64.getDecoder().decode(result);
    }

}


