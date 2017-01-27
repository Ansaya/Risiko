package Game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Static class to log events during execution
 */
public class Logger {

    private static Path out;

    public static void setOutPath(String OutPath) {
        out = Paths.get("." + File.separatorChar + (OutPath.endsWith(".txt") ? OutPath : OutPath.concat(".txt")));
    }

    private static Path err;

    public static void setErrPath(String ErrPath) {
        err = Paths.get("." + File.separatorChar + (ErrPath.endsWith(".txt") ? ErrPath : ErrPath.concat(".txt")));
    }

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    /**
     * Write text to standard output
     *
     * @param Text Text to write
     */
    public static void log(String Text) {
        if(out != null)
            writeLine(out, Text);
        System.out.println(Text);
    }

    /**
     * Write text to standard error
     *
     * @param Text Text to write
     */
    public static void err(String Text) {
        if(err != null)
            writeLine(err, Text);
        System.err.println(Text);
    }

    /**
     * Write text to standard error and append stack trace of given exception
     *
     * @param Text Text to write
     * @param e Exception to print stack trace of
     */
    public static void err(String Text, Exception e) {
        if(err != null)
            writeLine(err, Text + "\n" + stackTraceToString(e));

        System.err.println(Text);
        e.printStackTrace();
    }

    private static String stackTraceToString(Exception e) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }

    /**
     * Write Text to given file prepending date and time of writing
     *
     * @param path File path to write to
     * @param Text Text to write
     */
    private static void writeLine(Path path, String Text) {
        String entry = "\n" + LocalDateTime.now(ZoneId.of("Z")).format(dateFormatter) + "\t" +
                Text.replace("\n", "\r\n\t\t\t");

        while (entry.endsWith("\t"))
            entry = entry.substring(0, entry.length() - 2);

        if(!entry.endsWith("\r"))
            entry = entry + "\r";

        try {
            Files.write(path, entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
