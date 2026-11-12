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

    AppServices services();

    /** The open document, or {@code null} when nothing is open. */
    ReadOnlyObjectProperty<XmlDocumentModel> documentProperty();

    /** The element selected in the tree, or {@code null}. */
    ReadOnlyObjectProperty<Element> selectedElementProperty();

    /** True while a background task runs; bind your controls' disable state to it. */
    ReadOnlyBooleanProperty busyProperty();

    /** Adds {@code item} to the menu called {@code menuName}, creating that menu (before Help) if needed. */
    void addMenuItem(String menuName, MenuItem item);

    /** Adds a node to the main toolbar, after the built-in buttons. */
    void addToolbarItem(Node node);

    Dialogs dialogs();

    void setStatus(String message);

    /** Runs {@code work} off the UI thread, then {@code onSuccess} on it. Failures are reported in a dialog. */
    <T> void runTask(String description, Callable<T> work, Consumer<T> onSuccess);
}
