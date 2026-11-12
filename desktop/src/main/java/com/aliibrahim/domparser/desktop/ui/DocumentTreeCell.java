package com.aliibrahim.domparser.desktop.ui;

import com.aliibrahim.domparser.core.security.XmlEncryptionService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Renders an element as {@code tag  attr="value"  text} and allows editing the text of leaf elements in place.
 */
public final class DocumentTreeCell extends TreeCell<Element> {

    private final BiPredicate<Element, String> onEdit;
    private TextField editor;

    /** @param onEdit applies a new text value; returns false to keep the editor open (e.g. validation failed). */
    public DocumentTreeCell(BiPredicate<Element, String> onEdit) {
        this.onEdit = onEdit;
        getStyleClass().add("document-tree-cell");
    }

    public static boolean isEditableLeaf(Element element) {
        return element.getChildren().isEmpty() && !isEncrypted(element);
    }

    public static boolean isEncrypted(Element element) {
        return XmlEncryptionService.isWithinEncryptedData(element);
    }

    @Override
    protected void updateItem(Element element, boolean empty) {
        super.updateItem(element, empty);
        setText(null);
        if (empty || element == null) {
            setGraphic(null);
            return;
        }
        if (isEditing() && editor != null) {
            setGraphic(editor);
            return;
        }
        setGraphic(render(element));
    }

    private HBox render(Element element) {
        HBox box = new HBox(6);
        box.getStyleClass().add("node");
        if (XmlEncryptionService.isEncryptedData(element)) {
            Label lock = new Label("", Icon.LOCK.node());
            Label tag = new Label("encrypted " + (element.getAttributeValue("Type", "").endsWith("#Content")
                    ? "content" : "element"));
            tag.getStyleClass().addAll("tag", "encrypted");
            box.getChildren().addAll(lock, tag);
            return box;
        }
        Label tag = new Label(element.getQualifiedName());
        tag.getStyleClass().add("tag");
        box.getChildren().add(tag);
        if (!element.getAttributes().isEmpty()) {
            Label attrs = new Label(element.getAttributes().stream()
                    .map(this::formatAttribute)
                    .collect(Collectors.joining(" ")));
            attrs.getStyleClass().add("attrs");
            box.getChildren().add(attrs);
        }
        if (element.getChildren().isEmpty()) {
            String text = element.getTextTrim();
            Label value = new Label(text.isEmpty() ? "(empty)" : text);
            value.getStyleClass().add(text.isEmpty() ? "value-empty" : "value");
            box.getChildren().add(value);
        }
        return box;
    }

    private String formatAttribute(Attribute a) {
        return a.getQualifiedName() + "=\"" + a.getValue() + "\"";
    }

    @Override
    public void startEdit() {
        Element element = getItem();
        if (element == null || !isEditableLeaf(element) || !getTreeView().isEditable()) {
            return;
        }
        super.startEdit();
        editor = new TextField(element.getTextTrim());
        editor.getStyleClass().add("tree-editor");
        editor.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                e.consume();
            }
        });
        editor.setOnAction(e -> {
            if (onEdit.test(element, editor.getText())) {
                commitEdit(element);
            }
        });
        editor.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused && isEditing()) {
                cancelEdit();
            }
        });
        HBox box = new HBox(6, label(element.getName()), editor);
        box.getStyleClass().add("node");
        setGraphic(box);
        editor.requestFocus();
        editor.selectAll();
    }

    private static Label label(String name) {
        Label l = new Label(name);
        l.getStyleClass().add("tag");
        return l;
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        editor = null;
        updateItem(getItem(), getItem() == null);
    }

    @Override
    public void commitEdit(Element newValue) {
        super.commitEdit(newValue);
        editor = null;
        updateItem(newValue, newValue == null);
    }
}
