package Server;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Created by fiore on 27/01/2017.
 */
public class Logger {

    private static final Path out;

    private static final Path err;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    static {
        Path outPath = null, errPath = null;

        try {
            outPath = Files.createFile(Paths.get("./stdout.txt"));
            errPath = Files.createFile(Paths.get("./stderr.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        out = outPath;
        err = errPath;
    }

    /**
     * Write text to standard output
     *
     * @param Text Text to write
     */
    public static void log(String Text) {
        writeLine(out, Text);
        System.out.println(Text);
    }

    /**
     * Write text to standard error
     *
     * @param Text Text to write
     */
    public static void err(String Text) {
        writeLine(err, Text);
        System.err.println(Text);
    }

    /**
     * Write Text to given file prepending date and time of writing
     *
     * @param path File path to write to
     * @param Text Text to write
     */
    private static void writeLine(Path path, String Text) {
        final String entry = dateFormat.format(LocalDateTime.now(ZoneId.of("ETC"))) + "\t" +
                Text.replace("\n", "\n\t");

        try {
            Files.write(path, entry.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
