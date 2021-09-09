package sample;

import DOMParser.DOMXmlParser;
import DOMParser.Parser;
import Model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jdom2.JDOMException;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class Controller {
    public Label element_label = new Label();
    public Button save = new Button();
    public ComboBox<String> elements = new ComboBox<>();
    public ComboBox<String> gender_ = new ComboBox<>();
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
    public TreeView<String> dom_tree = new TreeView<>();
    public Button encrypt = new Button();
    public Button delete = new Button();
    public static Pane root_pane = new Pane();
    public TextField search_text_field = new TextField();
    public Button search_btn = new Button();
    public Button next = new Button();
    public Button previous = new Button();
    TreeItem<String> root;
    Parser parser = new DOMXmlParser();
    Process process = new Process(this);
    List<User> users = new ArrayList<>();

    private static int id = 0;
    private static boolean tree_changed = false;

    boolean flag = true;
    String message = "";
    int selected_node = 0;
    public static Stage primary_stage;
    int node_index = -1, leaf_index = -1;
    boolean node_flag = false, element_flag = false, doc_flag = false;

    public void on_read_file_clicked(ActionEvent event) {
        final File file;
        String path = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));
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
                root = new TreeItem<>("Users");
                List<String> strings = new ArrayList<>();
                for (User user : users) {
                    TreeItem<String> user_item = get_user_item(user);
                    id = user.getId();
                    root.getChildren().add(user_item);
                    strings.add("user " + user.getId());
                }
                Platform.runLater(() -> dom_tree.setRoot(root));
                elements.setItems(FXCollections.observableArrayList(strings));
                strings = new ArrayList<>();
                strings.add("");
                strings.add("Male");
                strings.add("Female");
                gender_.setItems(FXCollections.observableArrayList(strings));
                enable();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th1.start();
        th2.start();
    }

    public static void show_alert(String text) {
        Alert alert;
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primary_stage.getOwner());
        alert.setContentText(text);
        Platform.runLater(alert::show);
    }

    private void enable() {
        add.disableProperty().setValue(false);
        save.disableProperty().setValue(false);
        update.disableProperty().setValue(false);
        delete.disableProperty().setValue(false);
        encrypt.disableProperty().setValue(false);
        search_btn.disableProperty().setValue(false);
        search_text_field.disableProperty().setValue(false);
        first_name_.editableProperty().setValue(true);
        lase_name_.editableProperty().setValue(true);
        age_.editableProperty().setValue(true);
        dom_tree.setEditable(true);
        dom_tree.setCellFactory(TextFieldTreeCell.forTreeView());
        // Set editing related event handlers (OnEditStart)
        dom_tree.setOnEditStart(this::editStart);
        dom_tree.setOnEditCommit(this::editCommit);

        //Selection model for tree

        dom_tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != dom_tree.getRoot()) {
                selected_node = dom_tree.getRoot().getChildren().indexOf(newValue);
                if (selected_node != -1) {
                    set_user(selected_node);
                    node_flag = true;
                    element_flag = false;
                } else {
                    selected_node = dom_tree.getRoot().getChildren().indexOf(newValue.getParent());
                    leaf_index = newValue.getParent().getChildren().indexOf(newValue);
                    element_flag = true;
                    node_flag = false;
                }
                doc_flag = false;
            } else {
                doc_flag = true;
                element_flag = false;
                node_flag = false;
            }
        });
    }


    private void editCommit(EditEvent event) {
        if (event.getTreeItem() == dom_tree.getRoot() || event.getTreeItem() == null)
            return;
        if (!root.getChildren().contains(event.getTreeItem())) {
            for (TreeItem<String> item : dom_tree.getRoot().getChildren()) {
                if (item.getChildren().contains(event.getTreeItem())) {
                    node_index = dom_tree.getRoot().getChildren().indexOf(item);
                    leaf_index = item.getChildren().indexOf(event.getTreeItem());
                    if (leaf_index == 3)
                        if (!event.getNewValue().toString().matches("[0-9]+")) {
                            flag = false;
                            node_index++;
                            message += "You have input a non numeric value in User-" + node_index + "\n";
                        } else {
                            flag = true;

                        }
                    break;
                }
            }
            if (flag) {
                parser.update_user_element(node_index, leaf_index, event.getNewValue() + "");
                users.get(node_index).set_value(leaf_index, event.getNewValue());
                set_user(node_index);
                tree_changed = true;
                parser.get_first_name();
            }
        }
    }

    private void editStart(TreeView.EditEvent event) {
        root = dom_tree.getRoot();

    }

    private TreeItem<String> get_user_item(User user) {
        TreeItem<String> user_item = new TreeItem<>("user " + user.getId());
        TreeItem<String> first_name = new TreeItem<>(user.getFirstName());
        TreeItem<String> last_name = new TreeItem<>(user.getLastName());
        TreeItem<String> age = new TreeItem<>(user.getGender());
        TreeItem<String> gender = new TreeItem<>(user.getAge() + "");
        user_item.getChildren().addAll(first_name, last_name, age, gender);
        return user_item;
    }

    public void on_add_clicked(ActionEvent event) {

        if (validate()) {
            User user = new User();
            user.setFirstName(first_name_.getText());
            user.setLastName(lase_name_.getText());
            user.setAge(Integer.parseInt(age_.getText()));
            user.setId(++id);
            user.setGender(gender_.valueProperty().get());
            TreeItem<String> user_item = get_user_item(user);
            dom_tree.getRoot().getChildren().add(user_item);
            parser.create_user_element(user);
            users.add(user);
            tree_changed = true;
            clearFields();
        }

    }


    public void on_delete_clicked(ActionEvent event) {
        parser.delete_user_element(selected_node);
        dom_tree.getRoot().getChildren().remove(selected_node);
        dom_tree.getSelectionModel().select(0);
        tree_changed = true;
    }

    private void clearFields() {
        first_name_.clear();
        lase_name_.clear();
        age_.clear();
        gender_.getSelectionModel().select(0);
    }

    private boolean validate() {
        return !first_name_.getText().equals("") && !lase_name_.getText().equals("")
                && !gender_.valueProperty().get().equals("") && !age_.getText().equals("");
    }

    public void on_save_clicked(ActionEvent event) {
        Alert alert;
        if (!flag) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(primary_stage.getOwner());
            alert.setContentText("There are errors in the dom tree please fix them:\n" + message);
            Platform.runLater(alert::show);
        } else if (tree_changed) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("XML file saved successfully!");
            alert.initOwner(primary_stage.getOwner());
            try {
                FileChooser.ExtensionFilter xml_files = new FileChooser.ExtensionFilter("XML3 files", "*.xml");
                FileChooser save_as = new FileChooser();
                String path = System.getProperty("user.dir");
                save_as.setInitialDirectory(new File(path));
                save_as.getExtensionFilters().addAll(xml_files);
                String file_name = save_as.showSaveDialog(null).getName();
                Thread th_process = new Thread(() -> {
                    try {
                        process.process();
                    } catch (InterruptedException ex) {
                        Alert error_alert = new Alert(Alert.AlertType.ERROR);
                        error_alert.setContentText("Error!\n" + Arrays.toString(ex.getStackTrace()));
                        Platform.runLater(error_alert::show);
                    }
                });

                Thread th_save_thread = new Thread(() -> {
                    try {
                        process.setParser(parser);
                        process.save_file(file_name, parser);
                        tree_changed = false;
                        Platform.runLater(alert::show);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                th_process.start();
                th_save_thread.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            show_alert("you haven't made any changes to the document!");
        }
    }

    public void on_element_selected(ActionEvent event) {
        int index = elements.getSelectionModel().getSelectedIndex();
        set_user(index);

    }

    private void set_user(int index) {
        User user = users.get(index);
        first_name_.setText(user.getFirstName());
        lase_name_.setText(user.getLastName());
        age_.setText(user.getAge() + "");
        gender_.getSelectionModel().select(user.getGender().equalsIgnoreCase("male") ? 1 : 2);
    }

    public static void set_primary_stage(Stage stage) {
        primary_stage = stage;
    }

    public static void close_window_event(WindowEvent t) {
        if (tree_changed) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getButtonTypes().remove(ButtonType.OK);
            alert.getButtonTypes().add(ButtonType.CANCEL);
            alert.getButtonTypes().add(ButtonType.YES);
            alert.setTitle("Quit application");
            alert.setContentText("Close without saving?");
            alert.initOwner(primary_stage.getOwner());
            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent()) {
                if (res.get().equals(ButtonType.CANCEL))
                    t.consume();
                else if (res.get().equals(ButtonType.YES) || res.get().equals(ButtonType.CLOSE))
                    primary_stage.close();
            }
        } else {
            primary_stage.close();
        }
    }

    public void on_encrypt_clicked(ActionEvent event) {
        try {
            if (doc_flag)
                parser.encrypt_doc();
            else if (node_flag)
                parser.encrypt_node(selected_node);
            else if (element_flag)
                parser.encrypt_element(selected_node, leaf_index);
            tree_changed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void on_search_btn_clicked(ActionEvent event) {
        assert !search_text_field.getText().equals("");
        int index = parser.search(search_text_field.getText());
        if (index != -1) {
            select_node(index);
            next.disableProperty().setValue(false);
            previous.disableProperty().setValue(false);
        } else {
            show_alert("No such user found");
        }
    }

    private void select_node(int index) {
        root = dom_tree.getRoot();
        for (TreeItem<String> treeItem : root.getChildren()) {
            if (dom_tree.getRoot().getChildren().indexOf(treeItem) == index) {
                dom_tree.getRoot().setExpanded(true);
                dom_tree.getRoot().getChildren().get(index).setExpanded(true);
                set_user(index);
            }
        }
    }

    public void next(ActionEvent actionEvent) {
        if (parser.getName_index() < parser.getNodeList().size()) {
            assert !search_text_field.getText().equals("");
            int index = parser.next(search_text_field.getText());
            if (index != -1) {
                select_node(index);
                next.disableProperty().setValue(false);
            } else {
                show_alert("No next user found");
                next.disableProperty().setValue(true);
            }
            previous.disableProperty().setValue(false);
        } else {
            show_alert("No next user found");
            next.disableProperty().setValue(true);
            previous.disableProperty().setValue(false);
        }
    }

    public void previous(ActionEvent actionEvent) {
        if (parser.getName_index() > 0) {
            assert !search_text_field.getText().equals("");
            int index = parser.previous(search_text_field.getText());
            if (index != -1) {
                select_node(index);
                previous.disableProperty().setValue(false);
            } else {
                show_alert("No previous user found");
                next.disableProperty().setValue(false);
                previous.disableProperty().setValue(true);
            }
        } else {
            show_alert("No previous user found");
            next.disableProperty().setValue(false);
            previous.disableProperty().setValue(true);
        }
    }
}