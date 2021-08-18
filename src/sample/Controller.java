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
    TreeItem<String> root;
    Parser parser = new DOMXmlParser(this);
    Process process = new Process(this);
    List<User> users = new ArrayList<>();

    private static int id = 0;
    private static boolean tree_changed = false;

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
                Platform.runLater(() -> {
                    dom_tree.setRoot(root);
                });
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

    int selected_node = 0;

    private void enable() {
        add.disableProperty().setValue(false);
        save.disableProperty().setValue(false);
        update.disableProperty().setValue(false);
        delete.disableProperty().setValue(false);
        encrypt.disableProperty().setValue(false);
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
            if ((TreeItem<String>) newValue != dom_tree.getRoot()) {
                selected_node = dom_tree.getRoot().getChildren().indexOf((TreeItem<String>) newValue);
                set_user(selected_node);
            }
        });
    }

    boolean flag = true;
    String message = "";

    private void editCommit(EditEvent event) {
        int node_index = -1, leaf_index = -1;
        if (event.getTreeItem() == dom_tree.getRoot() || event.getTreeItem() == null)
            return;
        if (!root.getChildren().contains(event.getTreeItem())) {
            for (TreeItem<String> item : dom_tree.getRoot().getChildren()) {
                if (item.getChildren().contains(event.getTreeItem())) {
                    node_index = dom_tree.getRoot().getChildren().indexOf(item);
                    leaf_index = item.getChildren().indexOf(event.getTreeItem());
                    if (leaf_index == 3)
                        if (!event.getNewValue().toString().matches("[0-9]+")) {
                            System.out.println(dom_tree.getRoot().getChildren().get(node_index).
                                    getChildren().get(leaf_index).getValue());
                            flag = false;
                            node_index++;
                            message += "You have input a non numeric value in User-" + node_index + "\n";
                        } else
                            flag = true;
                    break;
                }
            }
            if (flag) {
                parser.update_user_element(node_index, leaf_index, event.getNewValue() + "");
            }
        }
    }

    private void editStart(TreeView.EditEvent event) {
        System.out.println("Start editing " + event.getTreeItem());
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
        } else if (tree_changed) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("XML file saved successfully!");
            alert.initOwner(primary_stage.getOwner());
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
                    error_alert.show();
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
        gender_.getSelectionModel().select(user.getGender().toLowerCase().equals("male") ? 1 : 2);
    }

    public static Stage primary_stage;

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
            alert.setContentText(String.format("Close without saving?"));
            alert.initOwner(primary_stage.getOwner());
            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent()) {
                if (res.get().equals(ButtonType.CANCEL))
                    t.consume();
            }
        }
    }
}