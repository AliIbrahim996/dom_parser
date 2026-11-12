package com.aliibrahim.domparser.desktop.ui;

import com.aliibrahim.domparser.core.schema.FieldDefinition;
import com.aliibrahim.domparser.core.schema.RecordSchema;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds an edit form from a {@link RecordSchema}: one labelled input per {@link FieldDefinition}, with inline
 * validation messages. Adding a field to a schema automatically adds it here.
 */
public final class RecordForm {

    private final VBox container;
    private final Map<String, Control> inputs = new LinkedHashMap<>();
    private final Map<String, Label> errors = new LinkedHashMap<>();
    private RecordSchema schema;

    public RecordForm(VBox container) {
        this.container = container;
    }

    public void setSchema(RecordSchema schema) {
        this.schema = schema;
        inputs.clear();
        errors.clear();
        container.getChildren().clear();
        if (schema == null) {
            Label hint = new Label("This document has no registered schema, so there is no record form.\n\n"
                    + "You can still browse the tree, edit values in place (double-click) and encrypt nodes.");
            hint.setWrapText(true);
            hint.getStyleClass().add("form-hint");
            container.getChildren().add(hint);
            return;
        }
        for (FieldDefinition field : schema.fields()) {
            Label label = new Label(field.label() + (field.required() ? "" : " (optional)"));
            label.getStyleClass().add("field-label");
            Control input = createInput(field);
            input.setMaxWidth(Double.MAX_VALUE);
            label.setLabelFor(input);
            Label error = new Label();
            error.getStyleClass().add("field-error");
            error.setManaged(false);
            error.setVisible(false);
            VBox group = new VBox(4, label, input, error);
            group.getStyleClass().add("field-group");
            container.getChildren().add(group);
            inputs.put(field.name(), input);
            errors.put(field.name(), error);
        }
    }

    private Control createInput(FieldDefinition field) {
        return switch (field.type()) {
            case TEXT -> {
                TextField tf = new TextField();
                tf.setPromptText(field.label());
                yield tf;
            }
            case INTEGER -> {
                TextField tf = new TextField();
                tf.setPromptText("0");
                tf.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().matches("-?\\d{0,9}") ? c : null));
                yield tf;
            }
            case CHOICE -> {
                ComboBox<String> cb = new ComboBox<>();
                cb.getItems().setAll(field.options());
                cb.setPromptText("Select…");
                yield cb;
            }
        };
    }

    public void show(Map<String, String> values) {
        clearErrors();
        inputs.forEach((name, input) -> setValue(input, values.getOrDefault(name, "")));
    }

    public void clear() {
        clearErrors();
        inputs.values().forEach(input -> setValue(input, ""));
    }

    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        inputs.forEach((name, input) -> values.put(name, getValue(input)));
        return values;
    }

    public void showErrors(Map<String, String> fieldErrors) {
        clearErrors();
        fieldErrors.forEach((name, message) -> {
            Label label = errors.get(name);
            Control input = inputs.get(name);
            if (label != null) {
                label.setText(message);
                label.setManaged(true);
                label.setVisible(true);
                input.getStyleClass().add("invalid");
            }
        });
        fieldErrors.keySet().stream().map(inputs::get).filter(i -> i != null).findFirst().ifPresent(Node::requestFocus);
    }

    public void clearErrors() {
        errors.values().forEach(l -> {
            l.setManaged(false);
            l.setVisible(false);
        });
        inputs.values().forEach(i -> i.getStyleClass().remove("invalid"));
    }

    public void focusFirst() {
        inputs.values().stream().findFirst().ifPresent(Node::requestFocus);
    }

    public boolean hasSchema() {
        return schema != null;
    }

    @SuppressWarnings("unchecked")
    private static void setValue(Control input, String value) {
        if (input instanceof TextField tf) {
            tf.setText(value);
        } else if (input instanceof ComboBox<?> cb) {
            ComboBox<String> combo = (ComboBox<String>) cb;
            String match = combo.getItems().stream().filter(o -> o.equalsIgnoreCase(value)).findFirst().orElse(null);
            combo.setValue(match == null && !value.isEmpty() ? value : match);
        }
    }

    private static String getValue(Control input) {
        if (input instanceof TextField tf) {
            return tf.getText();
        }
        if (input instanceof ComboBox<?> cb) {
            return cb.getValue() == null ? "" : cb.getValue().toString();
        }
        return "";
    }
}
