package com.aliibrahim.domparser.desktop.feature;

import com.aliibrahim.domparser.core.document.XmlDocumentModel;
import com.aliibrahim.domparser.desktop.AppServices;
import com.aliibrahim.domparser.desktop.ui.Dialogs;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.jdom2.Element;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** What the main window exposes to {@link Feature}s. Features never touch the controller directly. */
public interface FeatureContext {

    /**
     * The shared application services.
     *
     * @return the services
     */
    AppServices services();

    /**
     * The open document.
     *
     * @return a property holding the document, or {@code null} when nothing is open
     */
    ReadOnlyObjectProperty<XmlDocumentModel> documentProperty();

    /**
     * The element selected in the tree.
     *
     * @return a property holding the selected element, or {@code null}
     */
    ReadOnlyObjectProperty<Element> selectedElementProperty();

    /**
     * Whether a background task is running; bind your controls' disabled state to it.
     *
     * @return the busy property
     */
    ReadOnlyBooleanProperty busyProperty();

    /**
     * Adds an item to a menu, creating the menu (before Help) if it does not exist yet.
     *
     * @param menuName the menu name, e.g. {@code "Tools"}
     * @param item     the item to append
     */
    void addMenuItem(String menuName, MenuItem item);

    /**
     * Adds a node to the main toolbar, after the built-in buttons.
     *
     * @param node the button or other control to add
     */
    void addToolbarItem(Node node);

    /**
     * Themed dialogs owned by the main window.
     *
     * @return the dialog helper
     */
    Dialogs dialogs();

    /**
     * Shows {@code message} in the status bar.
     *
     * @param message the text to show
     */
    void setStatus(String message);

    /**
     * Runs {@code work} off the UI thread, then {@code onSuccess} on it. Failures are reported in a dialog.
     *
     * @param <T>         the result type
     * @param description what the task does, shown in the status bar, e.g. {@code "Encrypting"}
     * @param work        the work to run in the background
     * @param onSuccess   receives the result on the UI thread
     */
    <T> void runTask(String description, Callable<T> work, Consumer<T> onSuccess);
}
