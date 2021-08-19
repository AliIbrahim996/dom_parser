package DOMParser;

import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.DOMOutputter;

public class DocumentConverter {

    /**
     * @param input org.w3c.Document
     * @return org.jdom2.Document after conversation
     * */
    public static org.jdom2.Document convertDOMtoJDOM(org.w3c.dom.Document input) {
        DOMBuilder builder = new DOMBuilder();
        org.jdom2.Document output = builder.build(input);
        return output;
    }

    /**
     * @param jdomDoc org.jdom2.Document
     * @return  org.w3c.dom.Document after conversation
     * */
    public static org.w3c.dom.Document convertJDOMToDOM(org.jdom2.Document jdomDoc) throws JDOMException {
        DOMOutputter outputter = new DOMOutputter();
        return outputter.output(jdomDoc);
    }
}