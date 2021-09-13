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
     * method to append document with a new user
     *
     * @param user instance
     */
    void create_user_element(User user);

    /**
     * method to delete user from the document accordion to an index
     *
     * @param index of the user
     */
    void delete_user_element(int index);

    /**
     * @param index
     * @throws Exception
     */
    void encrypt_node(int index) throws Exception;

    /**
     * @param user_index
     * @param property_index
     * @param new_value
     */
    void update_user_element(int user_index, int property_index, String new_value);
    List<User> getUserList();
    void encrypt_doc() throws JDOMException;
    void encrypt_element(int selected_node, int leaf_index) throws JDOMException;
    int search(String first_name);
    int next(String first_name);
    int previous(String first_name);
    int getName_index();
    void get_first_name();

    List<Element> getNodeList();
}
