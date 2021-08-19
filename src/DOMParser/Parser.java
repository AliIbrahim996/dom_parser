package DOMParser;

import Model.User;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.jdom2.JDOMException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface Parser {
    void parse(File xml_file) throws InterruptedException;


    void write_xml_file(File file) throws TransformerException;


    void create_user_element(User user);

    void delete_user_element(int index);

    void encrypt_node(int index) throws Exception;

    void update_user_element(int user_index, int property_index, String new_value);

    public List<User> getUserList();
}
