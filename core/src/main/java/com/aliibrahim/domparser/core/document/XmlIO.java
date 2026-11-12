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

    /** Static utility; not instantiable. */
    private XmlIO() {
    }

    /** A parser that rejects DOCTYPE declarations and never expands entities (prevents XXE). */
    private static SAXBuilder builder() {
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        builder.setExpandEntities(false);
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return builder;
    }

    /**
     * Parses an XML file.
     *
     * @param file the file to read
     * @return the parsed document
     * @throws IOException    if the file cannot be read
     * @throws JDOMException  if the content is not well-formed XML or contains a DOCTYPE
     */
    public static Document read(Path file) throws IOException, JDOMException {
        return builder().build(file.toFile());
    }

    /**
     * Parses XML from a string.
     *
     * @param xml the XML text
     * @return the parsed document
     * @throws IOException    if reading fails
     * @throws JDOMException  if the content is not well-formed XML or contains a DOCTYPE
     */
    public static Document parse(String xml) throws IOException, JDOMException {
        return builder().build(new StringReader(xml));
    }

    /**
     * Writes pretty-printed XML through a temporary file, so a failed write never truncates the target. The
     * temporary file is deleted if anything goes wrong.
     *
     * @param document the document to write
     * @param file     the target file; replaced if it exists
     * @throws IOException if writing or replacing the file fails
     */
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

    /**
     * Formats a document as pretty-printed XML.
     *
     * @param document the document to format
     * @return the XML text
     */
    public static String toString(Document document) {
        return new XMLOutputter(Format.getPrettyFormat()).outputString(document);
    }
}
