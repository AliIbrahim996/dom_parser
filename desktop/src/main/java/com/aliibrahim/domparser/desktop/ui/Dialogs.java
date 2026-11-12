package com.aliibrahim.domparser.desktop.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Supplier;

/** Themed, owner-aware dialogs. */
public final class Dialogs {

    /** The answer to {@link #unsavedChanges(String)}. */
    public enum UnsavedChoice {
        /** Save first, then continue. */
        SAVE,
        /** Continue without saving. */
        DISCARD,
        /** Stay with the current document. */
        CANCEL
    }

    private final Supplier<Window> owner;

    /**
     * Creates the helper.
     *
     * @param owner supplies the window that owns the dialogs; may supply {@code null} before the stage exists
     */
    public Dialogs(Supplier<Window> owner) {
        this.owner = owner;
    }

    /**
     * Shows an information dialog and waits until it is closed.
     *
     * @param header  the headline
     * @param content the explanation
     */
    public void info(String header, String content) {
        alert(Alert.AlertType.INFORMATION, header, content).showAndWait();
    }

    /**
     * Shows an error dialog and waits until it is closed.
     *
     * @param header  the headline
     * @param content the explanation
     */
    public void error(String header, String content) {
        alert(Alert.AlertType.ERROR, header, content).showAndWait();
    }

    /**
     * Shows an error dialog for an exception: the root cause's message, with the stack trace in an expandable
     * section. Waits until it is closed.
     *
     * @param header the headline
     * @param error  the exception to report
     */
    public void error(String header, Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getMessage() == null) {
            root = root.getCause();
        }
        Alert alert = alert(Alert.AlertType.ERROR, header,
                root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName());
        StringWriter trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        TextArea details = new TextArea(trace.toString());
        details.setEditable(false);
        details.getStyleClass().add("xml-preview");
        details.setPrefRowCount(12);
        alert.getDialogPane().setExpandableContent(details);
        alert.showAndWait();
    }

    /**
     * Asks the user to confirm an action.
     *
     * @param header       the question
     * @param content      what will happen
     * @param confirmLabel text of the confirming button, e.g. {@code "Delete"}
     * @return true only if the user clicked the confirming button
     */
    public boolean confirm(String header, String content, String confirmLabel) {
        ButtonType ok = new ButtonType(confirmLabel, ButtonBar.ButtonData.OK_DONE);
        Alert alert = alert(Alert.AlertType.CONFIRMATION, header, content);
        alert.getButtonTypes().setAll(ButtonType.CANCEL, ok);
        return alert.showAndWait().filter(ok::equals).isPresent();
    }

    /**
     * Asks whether to save changes before the document is closed or replaced.
     *
     * @param documentName the file name shown in the question
     * @return the choice; closing the dialog counts as {@link UnsavedChoice#CANCEL}
     */
    public UnsavedChoice unsavedChanges(String documentName) {
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType discard = new ButtonType("Don't save", ButtonBar.ButtonData.NO);
        Alert alert = alert(Alert.AlertType.WARNING, "Save changes to " + documentName + "?",
                "Your changes will be lost if you don't save them.");
        alert.getButtonTypes().setAll(discard, ButtonType.CANCEL, save);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
            return UnsavedChoice.CANCEL;
        }
        return result.get() == save ? UnsavedChoice.SAVE : UnsavedChoice.DISCARD;
    }

    /**
     * Applies the app stylesheet and the current theme to a dialog pane.
     *
     * @param pane the dialog pane to style
     */
    public void style(DialogPane pane) {
        Window window = owner.get();
        if (window != null && window.getScene() != null) {
            pane.getStylesheets().setAll(window.getScene().getStylesheets());
            if (window.getScene().getRoot().getStyleClass().contains("dark")) {
                pane.getStyleClass().add("dark");
            }
        }
    }

    /** Creates a themed alert owned by the main window. */
    private Alert alert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(owner.get());
        alert.setTitle("DOM Parser");
        alert.setHeaderText(header);
        alert.setContentText(content);
        style(alert.getDialogPane());
        return alert;
    }
}
