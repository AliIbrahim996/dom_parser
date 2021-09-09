package DOMParser;

import Model.User;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.List;

public interface Parser {
    void parse(File xml_file) throws InterruptedException;


    void write_xml_file(File file) throws TransformerException;


    void create_user_element(User user);

    void delete_user_element(int index);

    void encrypt_node(int index) throws Exception;

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
