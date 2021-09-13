package DOMParser;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.filter.ElementFilter;
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
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import Model.User;
import sample.Controller;

public class UserParser implements Parser {

    /*fields*/
    List<Object> userList;
    List<Element> nodeList;
    org.jdom2.Document document;
    int name_index = 0;
    List<Element> search_results;

    /*Methods*/
    public List<Object> get_Object_List() {
        return userList;
    }

    private String getTagName(int leaf_index) {
        switch (leaf_index) {
            case 0:
                return "firstName";
            case 1:
                return "lastName";
            case 2:
                return "gender";
            case 3:
                return "age";
        }
        return "";
    }

    public List<Element> getNodeList() {
        return nodeList;
    }

    @Override
    public void delete_element(int index) {
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
                document = new DOMBuilder().build(doc);
                nodeList = document.getRootElement().getChildren("User");
                userList = new ArrayList();
                for (Element element : nodeList) {
                    userList.add(getUser(element));
                }
                get_element();
                notify();
            } catch (SAXException | ParserConfigurationException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void get_element() {
        Element root = document.getRootElement();
        ElementFilter nameFilter = new ElementFilter("firstName");
        search_results = new ArrayList<>();
        Iterator<Element> elementIterator = root.getDescendants(nameFilter);
        while (elementIterator.hasNext()) {
            search_results.add(elementIterator.next());
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
        } catch (Exception ignored) {

        }
    }

    @Override
    public void create_element(java.lang.Object object) {
        User user = (User) object;
        Element element = new Element("User");
        element.setAttribute(new Attribute("id", user.getId() + ""));
        element.addContent(new Element("firstName").setText(user.getFirstName()));
        element.addContent(new Element("lastName").setText(user.getLastName()));
        element.addContent(new Element("age").setText(String.valueOf(user.getAge())));
        element.addContent(new Element("gender").setText(user.getGender()));
        document.getRootElement().addContent(element);
        nodeList = document.getRootElement().getChildren("User");
        userList.add(user);
        get_element();
    }

    @Override
    public void update_element(int _index, int property_index, String new_value) {
        String property = get_selected_property(property_index);
        if (property != null)
            document.getRootElement().getChildren("User").
                    get(_index).getChildren(property)
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
        d = Encryptor.encrypt_node(d, e);
        index++;
        Controller.show_alert("User node " + index + " has been encrypted successfully!");
        document = DocumentConverter.convertDOMtoJDOM(d);
    }

    @Override
    public void encrypt_doc() throws JDOMException {
        Document d = DocumentConverter.convertJDOMToDOM(document);
        d = Encryptor.encrypt_document(d);
        Controller.show_alert("Document has been encrypted successfully!");
        document = DocumentConverter.convertDOMtoJDOM(d);
    }

    @Override
    public void encrypt_element(int selected_node, int leaf_index) throws JDOMException {
        Document d = DocumentConverter.convertJDOMToDOM(document);
        org.w3c.dom.Element user_node = (org.w3c.dom.Element) d.getElementsByTagName("User").item(selected_node);
        org.w3c.dom.Element leaf_node = (org.w3c.dom.Element) user_node.getElementsByTagName(getTagName(leaf_index)).item(0);
        d = Encryptor.encrypt_node(d, leaf_node);
        selected_node++;
        Controller.show_alert(getTagName(leaf_index) + " of user " + selected_node + " has been encrypted successfully!");
        document = DocumentConverter.convertDOMtoJDOM(d);
    }


    @Override
    public int search(String element_value) {
        Element element;
        while (name_index < search_results.size()) {
            element = search_results.get(name_index);
            if (element.getValue().equals(element_value)) {
                return name_index++;
            }
            name_index++;
        }
        name_index = 0;
        return -1;
    }

    @Override
    public int next(String element_value) {
        if (name_index < search_results.size()) {
            int pre_index = name_index;
            Element element;
            while (name_index < search_results.size()) {
                element = search_results.get(name_index);
                if (element.getValue().equals(element_value)) {
                    return name_index++;
                }
                name_index++;
            }
            name_index = pre_index;
        }
        return -1;
    }

    @Override
    public int previous(String element_value) {
        if (name_index > 0) {
            int pre_index = name_index;
            Element element;
            name_index--;
            while (name_index-- > 0) {
                element = search_results.get(name_index);
                if (element.getValue().equals(element_value)) {
                    return name_index;
                }
            }
            name_index = pre_index;
        }
        return -1;
    }

    @Override
    public int get_index() {
        return name_index;
    }
}
