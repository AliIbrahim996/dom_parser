package DOMParser;

import Model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.List;

public interface Parser {
    void parse(File xml_file) throws InterruptedException;

    void deleteElement(Document doc);

    void updateElementValue(Document doc);

    void addElement(Document doc);

    void write_xml_file(Document doc) throws TransformerException;

    Node create_user_element(Document doc, String id, String firstName, String lastName, String age, String gender);

    public List<User> getUserList();

    NodeList getNodeList();
}
