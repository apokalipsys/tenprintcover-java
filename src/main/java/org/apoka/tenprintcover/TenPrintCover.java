package org.apoka.tenprintcover;

import org.apoka.graphics.Image;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

public class TenPrintCover {
    @Option(name = "-t", usage = "Book title", required = true, metaVar = "TITLE")
    String title;

    @Option(name = "-s", usage = "Book subtitle", metaVar = "SUBTITLE")
    String subtitle;

    @Option(name = "-a", usage = "Author(s) of the book", required = true, metaVar = "AUTHOR")
    String author;

    @Option(name = "-o", usage = "Filename of the cover image in PNG format", required = true, metaVar = "FILE")
    String outfile;

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);

            return;
        }

        if(subtitle == null) {
            subtitle = "";
        }

        Cover cover = new Cover(title, subtitle, author, 400, 600);

        Image image = cover.draw();
        image.save(outfile);
    }

    public static void main(String... args) throws IOException {
        new TenPrintCover().doMain(args);
    }
}
