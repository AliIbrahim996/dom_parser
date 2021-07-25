package sample;

import DOMParser.DOMXmlParser;
import DOMParser.Parser;
import Model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    public Label element_label = new Label();
    public Button save = new Button();
    public ComboBox<String> elements = new ComboBox();
    public TextArea _text = new TextArea();
    public Button add = new Button();
    public Button read_file = new Button();
    public Button update = new Button();
    public Label first_name_label = new Label();
    public Label last_name_label = new Label();
    public Label age_label = new Label();
    public Label gender_label = new Label();
    public TextField first_name_ = new TextField();
    public TextField lase_name_ = new TextField();
    public TextField age_ = new TextField();
    public TextField gender_ = new TextField();

    Parser parser = new DOMXmlParser(this);
    Process process = new Process(this);
    List<User> users = new ArrayList();

    public void on_read_file_clicked(ActionEvent event) {
        final File file;
        String path = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(path);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Xml files", "*.xml")
        );
        fileChooser.setTitle("Open XML file");
        file = fileChooser.showOpenDialog(null);

        Thread th1 = new Thread(() -> {
            try {
                process.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());

            }
        });

        Thread th2 = new Thread(() -> {
            try {
                parser.parse(file);
                users = parser.getUserList();
                List<String> strings = new ArrayList<>();
                for (User user : users) {
                    strings.add("user " + user.getId());
                }
                elements.setItems(FXCollections.observableArrayList(strings));
                add.disableProperty().setValue(false);
                save.disableProperty().setValue(false);
                update.disableProperty().setValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th1.start();
        th2.start();
    }

    public void on_add_clicked(ActionEvent event) {
    }


    public void on_update_clicked(ActionEvent event) {
    }

    public void on_save_clicked(ActionEvent event) {
        FileChooser.ExtensionFilter xml_files =
                new FileChooser.ExtensionFilter("XML files", "*.xml");
        FileChooser save_as = new FileChooser();
        save_as.getExtensionFilters().addAll(xml_files);
        String file_name = save_as.showSaveDialog(null).getName();
        Thread th_process = new Thread(() -> {
            try {
                process.process();
            } catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(null, "Error: " + Arrays.toString(ex.getStackTrace()));

            }
        });

        Thread th_save_thread = new Thread(() -> {
            try {
                process.setParser(parser);
                process.save_file(file_name);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th_process.start();
        th_save_thread.start();
    }

    public void on_element_selected(ActionEvent event) {
        int index = elements.getSelectionModel().getSelectedIndex();
        User user = users.get(index);
        first_name_.setText(user.getFirstName());
        lase_name_.setText(user.getLastName());
        age_.setText(user.getAge() + "");
        gender_.setText(user.getGender() + "");
    }


}
