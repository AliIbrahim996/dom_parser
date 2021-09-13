package DOMParser;

import Model.User;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.List;

public interface Parser {
    /**
     * method to read an xml file and store it in jdom2 document.
     *
     * @param xml_file instance
     * @throws InterruptedException
     */
    void parse(File xml_file) throws InterruptedException;

    /**
     * method to write jdom2 document to an xml file
     *
     * @param file instance
     * @throws TransformerException
     */
    void write_xml_file(File file) throws TransformerException;

    /**
     * method to append document with a new Object
     *
     * @param object instance
     */
    void create_element(java.lang.Object object);

    /**
     * method to delete Object  from the document accordion to an index
     *
     * @param index of the Object
     */
    void delete_element(int index);

    /**
     * @param index
     * @throws Exception
     */
    void encrypt_node(int index) throws Exception;

    /**
     * @param _index
     * @param property_index
     * @param new_value
     */
    void update_element(int _index, int property_index, String new_value);
    List<Object> get_Object_List();
    void encrypt_doc() throws JDOMException;
    void encrypt_element(int selected_node, int leaf_index) throws JDOMException;
    int search(String element_value);
    int next(String element_value);
    int previous(String element_value);
    int get_index();
    void get_element();

    List<Element> getNodeList();
}
