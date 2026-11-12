package com.aliibrahim.domparser.core.document;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.DOMOutputter;

import java.util.ArrayDeque;
import java.util.Deque;

/** Converts between JDOM (used for editing) and W3C DOM (needed by Apache Santuario). */
public final class DocumentConverter {

    /** Static utility; not instantiable. */
    private DocumentConverter() {
    }

    /**
     * Builds a JDOM copy of a W3C DOM document.
     *
     * @param dom the W3C document to convert
     * @return an independent JDOM document with the same content
     */
    public static org.jdom2.Document toJdom(org.w3c.dom.Document dom) {
        return new DOMBuilder().build(dom);
    }

    /**
     * Builds a W3C DOM copy of a JDOM document.
     *
     * @param jdom the JDOM document to convert
     * @return an independent W3C document with the same content
     * @throws IllegalStateException if the document cannot be converted
     */
    public static org.w3c.dom.Document toDom(org.jdom2.Document jdom) {
        try {
            return new DOMOutputter().output(jdom);
        } catch (JDOMException e) {
            throw new IllegalStateException("Could not convert document", e);
        }
    }

    /**
     * Finds the W3C element at the same position as {@code jdomElement} in its JDOM tree. Use it on a document
     * produced by {@link #toDom(org.jdom2.Document)} from that tree.
     *
     * @param dom         the converted W3C document
     * @param jdomElement an element of the original JDOM document
     * @return the corresponding W3C element
     * @throws IllegalStateException if the trees do not have the same shape
     */
    public static org.w3c.dom.Element locate(org.w3c.dom.Document dom, Element jdomElement) {
        Deque<Integer> path = new ArrayDeque<>();
        for (Element e = jdomElement; e.getParentElement() != null; e = e.getParentElement()) {
            path.push(e.getParentElement().getChildren().indexOf(e));
        }
        org.w3c.dom.Element current = dom.getDocumentElement();
        for (int index : path) {
            current = childElement(current, index);
        }
        return current;
    }

    /**
     * Returns the {@code index}-th element child of {@code parent}, skipping text and other non-element nodes.
     *
     * @throws IllegalStateException if {@code parent} has fewer element children
     */
    private static org.w3c.dom.Element childElement(org.w3c.dom.Element parent, int index) {
        int seen = 0;
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof org.w3c.dom.Element el && seen++ == index) {
                return el;
            }
        }
        throw new IllegalStateException("Element path does not exist in converted document");
    }
}
