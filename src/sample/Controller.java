package sample;

import DOMParser.DOMXmlParser;
import DOMParser.Parser;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

public class Controller {
    public Label element_label = new Label();
    public Button save = new Button();
    public ComboBox elements = new ComboBox();
    public TextArea _text = new TextArea();
    public Button add = new Button();
    public Button read_file = new Button();
    public Button update = new Button();
    public Label first_name_label = new Label();
    public Label last_name_label = new Label();
    public Label age_label = new Label();
    public Label gender_label = new Label();
    public TextField first_name_;
    public TextField lase_name_;
    public TextField age_;
    public TextField gender_;

    Parser parser = new DOMXmlParser();

    public void on_read_file_clicked(ActionEvent event) {
    }

    public void on_add_clicked(ActionEvent event) {
    }


    public void on_update_clicked(ActionEvent event) {
    }

    public void on_save_clicked(ActionEvent event) {
    }

    public void on_element_selected(ActionEvent event) {
    }


}
