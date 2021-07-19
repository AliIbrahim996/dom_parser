package DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import java.io.File;

public interface Parser {
    public void parse(File xml_file);

    public void deleteElement(Document doc);

    public void updateElementValue(Document doc);

    public void addElement(Document doc);

    public void write_xml_file(Document doc) throws TransformerException;

    public Node create_user_element(Document doc, String id, String firstName, String lastName, String age, String gender);
}
