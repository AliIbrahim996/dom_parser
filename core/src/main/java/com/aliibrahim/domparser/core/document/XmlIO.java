package com.aliibrahim.domparser.core.document;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Reading and writing JDOM documents, configured safely (no DOCTYPEs, no external entities). */
public final class XmlIO {

    private XmlIO() {
    }

    private static SAXBuilder builder() {
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        builder.setExpandEntities(false);
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return builder;
    }

    public static Document read(Path file) throws IOException, JDOMException {
        return builder().build(file.toFile());
    }

    public static Document parse(String xml) throws IOException, JDOMException {
        return builder().build(new StringReader(xml));
    }

    /** Writes pretty-printed XML via a temp file so a failed write never truncates the target. */
    public static void write(Document document, Path file) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path tmp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(tmp)) {
                new XMLOutputter(Format.getPrettyFormat()).output(document, out);
            }
            Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    public static String toString(Document document) {
        return new XMLOutputter(Format.getPrettyFormat()).outputString(document);
    }
}
