package app;

import ui.LivrariaCLI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Application {

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("./data.txt")).stream()
                .map(line -> line.split(","))
                .map(csv -> String.join("\n", csv) + "\n")
                .toList();
        final byte[] data = String.join("\n", lines).getBytes(StandardCharsets.UTF_8);
        InputStream bookData = new ByteArrayInputStream(data);
        SequenceInputStream sequenceInputStream = new SequenceInputStream(bookData, System.in);
        LivrariaCLI cli = new LivrariaCLI(sequenceInputStream, System.out);
        cli.repl();
        cli.close();
    }
}
