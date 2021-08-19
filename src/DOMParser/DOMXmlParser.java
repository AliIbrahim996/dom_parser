package DOMParser;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import Model.User;
import sample.Controller;

public class DOMXmlParser implements Parser {
    private Controller controller;

    public DOMXmlParser(Controller controller) {
        this.controller = controller;
    }

    List<User> userList;
    List<Element> nodeList;
    org.jdom2.Document document;

    public List<User> getUserList() {
        return userList;
    }

    public List<Element> getNodeList() {
        return nodeList;
    }

    public org.jdom2.Document getDoc() {
        return document;
    }

    @Override
    public void delete_user_element(int index) {
        document.getRootElement().getChildren("User").remove(index);
    }

    @Override
    public void parse(File xml_file) throws InterruptedException {
        Thread.sleep(2000);
        synchronized (this) {
            DocumentBuilderFactory db_factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder d_builder;
            try {
                d_builder = db_factory.newDocumentBuilder();
                Document doc = d_builder.parse(xml_file);
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                document = new DOMBuilder().build(doc);
                nodeList = document.getRootElement().getChildren("User");
                userList = new ArrayList();
                for (Element element : nodeList) {
                    userList.add(getUser(element));
                }
                for (User emp : userList) {
                    System.out.println(emp.toString());
                }
                notify();
            } catch (SAXException | ParserConfigurationException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static User getUser(Element node) {
        User user = new User();
        user.setId(Integer.parseInt(node.getAttributeValue("id")));
        user.setFirstName(node.getChild("firstName").getValue());
        user.setLastName(node.getChild("lastName").getValue());
        user.setGender(node.getChild("gender").getValue());
        user.setAge(Integer.parseInt(node.getChild("age").getValue()));
        return user;
    }

    @Override
    public void write_xml_file(File file) {
        XMLOutputter xmlOutputter = new XMLOutputter();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            xmlOutputter.setFormat(Format.getPrettyFormat());
            xmlOutputter.output(document, fileOutputStream);
            System.out.println("XML file updated successfully");
        } catch (Exception ignored) {

        }
    }

    @Override
    public void create_user_element(User user) {
        Element element = new Element("User");
        element.setAttribute(new Attribute("id", user.getId() + ""));
        element.addContent(new Element("firstName").setText(user.getFirstName()));
        element.addContent(new Element("lastName").setText(user.getLastName()));
        element.addContent(new Element("age").setText(String.valueOf(user.getAge())));
        element.addContent(new Element("gender").setText(user.getGender()));
        document.getRootElement().addContent(element);
    }

    @Override
    public void update_user_element(int user_index, int property_index, String new_value) {
        String property = get_selected_property(property_index);
        if (property != null)
            document.getRootElement().getChildren("User").
                    get(user_index).getChildren(property)
                    .get(0).setContent(new Text(new_value));
    }

    private String get_selected_property(int property_index) {
        switch (property_index) {
            case 0:
                return "firstName";
            case 1:
                return "lastName";
            case 2:
                return "gender";
            case 3:
                return "age";
            default:
                return null;
        }
    }

    @Override
    public void encrypt_node(int index) throws Exception {
        Document d = DocumentConverter.convertJDOMToDOM(document);
        org.w3c.dom.Element e = (org.w3c.dom.Element) d.getElementsByTagName("User").item(index);
        //Encrypt element at specified index
        d = Encryptor.encrypt_element(d, e);
        index++;
        Controller.show_alert("User node " + index + " has been encrypted successfully!");
        document = DocumentConverter.convertDOMtoJDOM(d);
    }
}
