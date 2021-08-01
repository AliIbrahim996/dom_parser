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

    void addElement(Document doc, String element_name, String element_value, int index);

    void write_xml_file(Document doc) throws TransformerException;

    Node create_user_element(Document doc, String id, String firstName, String lastName, String age, String gender);

    void create_user_element(User user);

    public List<User> getUserList();

    NodeList getNodeList();
}
