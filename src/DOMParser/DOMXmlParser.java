package DOMParser;

import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import Model.User;
import sample.Controller;

public class DOMXmlParser implements Parser {
    private Controller controller;

    public DOMXmlParser(Controller controller) {
        this.controller = controller;
    }

    List<User> userList;
    NodeList nodeList;
    Document doc;


    public List<User> getUserList() {
        return userList;
    }

    public NodeList getNodeList() {
        return nodeList;
    }

    @Override
    public void parse(File xml_file) throws InterruptedException {
        Thread.sleep(2000);
        synchronized (this) {
            DocumentBuilderFactory db_factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder d_builder;
            try {
                d_builder = db_factory.newDocumentBuilder();
                doc = d_builder.parse(xml_file);
                doc.getDocumentElement().normalize();
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                nodeList = doc.getElementsByTagName("User");

                //XML is loaded as Document in memory,convert it to Object List
                userList = new ArrayList();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    userList.add(getUser(nodeList.item(i)));
                }
                //print User list information
                for (User emp : userList) {
                    System.out.println(emp.toString());
                }
                notify();
            } catch (SAXException | ParserConfigurationException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static User getUser(Node node) {
        User user = new User();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            user.setId(Integer.parseInt(getTagValue("id", element)));
            user.setFirstName(getTagValue("firstName", element));
            user.setLastName(getTagValue("lastName", element));
            user.setGender(getTagValue("gender", element));
            user.setAge(Integer.parseInt(getTagValue("age", element)));
        }
        return user;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }


    @Override
    public void write_xml_file(Document doc) throws TransformerFactoryConfigurationError, TransformerException {
        doc.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File("users_updated.xml"));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        System.out.println("XML file updated successfully");
    }


    /**
     * Add a new element salary to user element.
     *
     * @param doc
     */
    @Override
    public void addElement(Document doc, String element_name, String element_value, int index) {
        NodeList users = doc.getElementsByTagName("User");
        Element emp = (Element) users.item(index);
        Element salaryElement = doc.createElement(element_name);
        salaryElement.appendChild(doc.createTextNode(element_value));
        emp.appendChild(salaryElement);
    }

    /**
     * Delete gender element from User element
     *
     * @param doc
     */
    @Override
    public void deleteElement(Document doc) {
        NodeList users = doc.getElementsByTagName("User");
        Element user = null;
        // loop for each user
        for (int i = 0; i < users.getLength(); i++) {
            user = (Element) users.item(i);
            Node genderNode = user.getElementsByTagName("gender").item(0);
            user.removeChild(genderNode);
        }

    }

    /**
     * Update firstName element value to Upper case.
     *
     * @param doc
     */
    @Override
    public void updateElementValue(Document doc) {
        NodeList users = doc.getElementsByTagName("User");
        // loop for each user
        for (int i = 0; i < users.getLength(); i++) {
            Element user = (Element) users.item(i);
            Node name = user.getElementsByTagName("firstName").item(0).getFirstChild();
            name.setNodeValue(name.getNodeValue().toUpperCase());
        }
    }

    @Override
    public Node create_user_element(Document doc, String id, String firstName, String lastName, String age, String gender) {
        Element user = doc.createElement("User");

        // set id attribute
        user.setAttribute("id", id);

        // create firstName element
        user.appendChild(createUserElements(doc, user, "firstName", firstName));

        // create lastName element
        user.appendChild(createUserElements(doc, user, "lastName", lastName));

        // create age element
        user.appendChild(createUserElements(doc, user, "age", age));

        // create gender element
        user.appendChild(createUserElements(doc, user, "gender", gender));

        return user;
    }

    @Override
    public void create_user_element(User user) {
        Node new_user = create_user_element(doc, user.getId() + "", user.getFirstName(), user.getLastName(),
                user.getAge() + "", user.getGender());
        doc.getDocumentElement().appendChild(new_user);
    }

    // utility method to create text node
    private static Node createUserElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

}
