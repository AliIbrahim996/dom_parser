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

    /**
     * Creates a cell.
     *
     * @param onEdit applies a new text value to an element; returns false to keep the editor open (e.g. when
     *               validation failed)
     */
    public DocumentTreeCell(BiPredicate<Element, String> onEdit) {
        this.onEdit = onEdit;
        getStyleClass().add("document-tree-cell");
    }

    /**
     * Whether the element's text can be edited in place: it has no child elements and is not encrypted.
     *
     * @param element the element to test
     * @return true for an editable leaf
     */
    public static boolean isEditableLeaf(Element element) {
        return element.getChildren().isEmpty() && !isEncrypted(element);
    }

    /**
     * Whether the element is encrypted or sits inside encrypted content.
     *
     * @param element the element to test
     * @return true when it is or is inside an {@code EncryptedData} element
     */
    public static boolean isEncrypted(Element element) {
        return XmlEncryptionService.isWithinEncryptedData(element);
    }

    /** Shows the editor while editing, otherwise the rendered element. */
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

    /** Builds the cell graphic: a lock for encrypted data, otherwise tag, attributes and (for leaves) the text. */
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

    /** Formats an attribute as {@code name="value"}. */
    private String formatAttribute(Attribute a) {
        return a.getQualifiedName() + "=\"" + a.getValue() + "\"";
    }

    /**
     * Opens an inline text editor for editable leaves. Enter applies the value through the edit callback, and
     * Escape or leaving the field cancels.
     */
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

    /** A label styled as an element tag. */
    private static Label label(String name) {
        Label l = new Label(name);
        l.getStyleClass().add("tag");
        return l;
    }

    /** Closes the editor without applying the value. */
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        editor = null;
        updateItem(getItem(), getItem() == null);
    }

    /** Closes the editor after the value was applied. */
    @Override
    public void commitEdit(Element newValue) {
        super.commitEdit(newValue);
        editor = null;
        updateItem(newValue, newValue == null);
    }
}
