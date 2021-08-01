package DOMParser;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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
                //XML is loaded as Document in memory,convert it to Object List
                userList = new ArrayList();
                for (Element element : nodeList) {
                    userList.add(getUser(element));
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
        element.addContent(new Element("lastName").setText(user.getFirstName()));
        element.addContent(new Element("age").setText(String.valueOf(user.getAge())));
        element.addContent(new Element("gender").setText(user.getFirstName()));
        document.getRootElement().addContent(element);
    }
}
