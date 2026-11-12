package com.aliibrahim.domparser.desktop.ui;

import com.aliibrahim.domparser.core.document.DocumentChange;
import com.aliibrahim.domparser.core.document.DocumentListener;
import com.aliibrahim.domparser.core.document.RecordNode;
import com.aliibrahim.domparser.core.document.ValidationException;
import com.aliibrahim.domparser.core.document.XmlDocumentModel;
import com.aliibrahim.domparser.core.document.XmlIO;
import com.aliibrahim.domparser.core.schema.FieldDefinition;
import com.aliibrahim.domparser.core.schema.RecordSchema;
import com.aliibrahim.domparser.core.search.SearchCursor;
import com.aliibrahim.domparser.core.search.SearchMatch;
import com.aliibrahim.domparser.core.security.XmlEncryptionService;
import com.aliibrahim.domparser.desktop.AppServices;
import com.aliibrahim.domparser.desktop.feature.Feature;
import com.aliibrahim.domparser.desktop.feature.FeatureContext;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jdom2.Element;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Controller for {@code main-view.fxml}. Owns the open {@link XmlDocumentModel} and keeps the tree, table, form,
 * XML preview and status bar in sync with it. Extra functionality plugs in through {@link FeatureContext}.
 */
public final class MainController implements FeatureContext {

    private static final String PREF_DARK = "theme.dark";
    private static final String PREF_LAST_DIR = "files.lastDirectory";

    private final AppServices services;
    private final ReadOnlyObjectWrapper<XmlDocumentModel> document = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<RecordNode> selectedRecord = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper selectedRecordEncrypted = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper hasMatches = new ReadOnlyBooleanWrapper();
    private final Map<Element, TreeItem<Element>> treeIndex = new IdentityHashMap<>();
    private final DocumentListener documentListener = this::onDocumentChanged;

    private Stage stage;
    private Dialogs dialogs;
    private RecordForm form;
    private SearchCursor searchCursor;
    private boolean syncingSelection;

    // ---- FXML ---------------------------------------------------------------------------------------------------
    @FXML private BorderPane root;
    @FXML private MenuBar menuBar;
    @FXML private Menu helpMenu;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem newRecordMenuItem;
    @FXML private MenuItem deleteRecordMenuItem;
    @FXML private MenuItem findNextMenuItem;
    @FXML private MenuItem findPreviousMenuItem;
    @FXML private CheckMenuItem darkThemeMenuItem;
    @FXML private ToolBar toolBar;
    @FXML private Pane toolbarSpacer;
    @FXML private Button newDocumentButton;
    @FXML private Button openButton;
    @FXML private Button saveButton;
    @FXML private Button newRecordButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchField;
    @FXML private Label searchIcon;
    @FXML private Label searchCountLabel;
    @FXML private Button searchPreviousButton;
    @FXML private Button searchNextButton;
    @FXML private SplitPane workspace;
    @FXML private VBox emptyState;
    @FXML private TreeView<Element> documentTree;
    @FXML private Label treeSubtitle;
    @FXML private TabPane centerTabs;
    @FXML private Tab xmlTab;
    @FXML private TableView<RecordNode> recordTable;
    @FXML private TextArea xmlPreview;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private VBox formContainer;
    @FXML private Button clearButton;
    @FXML private Button addButton;
    @FXML private Button applyButton;
    @FXML private ProgressIndicator busyIndicator;
    @FXML private Label statusLabel;
    @FXML private Label encryptedLabel;
    @FXML private Label schemaLabel;
    @FXML private Label countLabel;
    @FXML private Label dirtyLabel;

    /**
     * Creates the controller; FXML fields are injected when the view is loaded.
     *
     * @param services the shared application services
     */
    public MainController(AppServices services) {
        this.services = services;
    }

    /** Called by FXMLLoader: sets icons, cell factories, listeners and bindings, and shows the empty state. */
    @FXML
    private void initialize() {
        form = new RecordForm(formContainer);
        dialogs = new Dialogs(() -> stage);

        newDocumentButton.setGraphic(Icon.FILE.node());
        openButton.setGraphic(Icon.OPEN.node());
        saveButton.setGraphic(Icon.SAVE.node());
        newRecordButton.setGraphic(Icon.ADD.node());
        deleteButton.setGraphic(Icon.DELETE.node());
        searchIcon.setGraphic(Icon.SEARCH.node());
        searchPreviousButton.setGraphic(Icon.UP.node());
        searchNextButton.setGraphic(Icon.DOWN.node());

        documentTree.setEditable(true);
        documentTree.setCellFactory(tv -> new DocumentTreeCell(this::editElementText));
        documentTree.getSelectionModel().selectedItemProperty().addListener((obs, old, item) ->
                selectedElement.set(item == null ? null : item.getValue()));

        selectedElement.addListener((obs, old, element) -> {
            XmlDocumentModel model = document.get();
            selectedRecord.set(model == null ? null : model.recordOf(element).orElse(null));
        });
        selectedRecord.addListener((obs, old, record) -> showRecord(record));

        recordTable.setPlaceholder(new Label("No records yet"));
        recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        recordTable.getSelectionModel().selectedItemProperty().addListener((obs, old, record) -> {
            if (!syncingSelection && record != null) {
                selectInTree(record.element());
            }
        });

        searchField.textProperty().addListener((obs, old, text) -> updateSearch(text));
        searchField.setOnAction(e -> onSearchNext());

        xmlTab.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                refreshXmlPreview();
            }
        });

        bindEnablement();
        setDocument(null);
    }

    /** Binds visibility and the disabled state of controls to the document, selection, search and busy state. */
    private void bindEnablement() {
        BooleanBinding noDocument = document.isNull();
        BooleanBinding noSchema = Bindings.createBooleanBinding(
                () -> document.get() == null || document.get().schema().isEmpty(), document);
        BooleanBinding noRecord = selectedRecord.isNull();

        workspace.disableProperty().bind(busy);
        workspace.visibleProperty().bind(noDocument.not());
        emptyState.visibleProperty().bind(noDocument);
        busyIndicator.visibleProperty().bind(busy);
        busyIndicator.managedProperty().bind(busy);

        saveButton.disableProperty().bind(noDocument.or(busy));
        saveMenuItem.disableProperty().bind(noDocument.or(busy));
        saveAsMenuItem.disableProperty().bind(noDocument.or(busy));
        newRecordButton.disableProperty().bind(noSchema.or(busy));
        newRecordMenuItem.disableProperty().bind(noSchema.or(busy));
        deleteButton.disableProperty().bind(noRecord.or(busy));
        deleteRecordMenuItem.disableProperty().bind(noRecord.or(busy));
        addButton.disableProperty().bind(noSchema);
        clearButton.disableProperty().bind(noSchema);
        applyButton.disableProperty().bind(noRecord.or(selectedRecordEncrypted));
        searchField.disableProperty().bind(noSchema);
        BooleanBinding noMatches = hasMatches.not();
        searchPreviousButton.disableProperty().bind(noSchema.or(noMatches));
        searchNextButton.disableProperty().bind(noSchema.or(noMatches));
        findNextMenuItem.disableProperty().bind(noSchema.or(noMatches));
        findPreviousMenuItem.disableProperty().bind(noSchema.or(noMatches));
    }

    /**
     * Connects the controller to its window: the close prompt, theme, drag-and-drop and features. Called once
     * by the application after the view is loaded.
     *
     * @param stage    the main window
     * @param features the features to install, in order; one that fails to install is skipped and logged
     */
    public void attach(Stage stage, List<Feature> features) {
        this.stage = stage;
        stage.setOnCloseRequest(e -> {
            if (!confirmDiscardChanges()) {
                e.consume();
            }
        });
        darkThemeMenuItem.setSelected(services.preferences().getBoolean(PREF_DARK, false));
        applyTheme();
        installDragAndDrop();
        for (Feature feature : features) {
            try {
                feature.install(this);
            } catch (RuntimeException e) {
                System.err.println("Feature '" + feature.id() + "' failed to install: " + e);
            }
        }
        updateTitle();
    }

    // ---- document lifecycle ---------------------------------------------------------------------------------------

    /** Makes {@code model} the open document (or none), resetting search, form, table and tree. */
    private void setDocument(XmlDocumentModel model) {
        XmlDocumentModel old = document.get();
        if (old != null) {
            old.removeListener(documentListener);
        }
        searchField.clear();
        searchCursor = null;
        hasMatches.set(false);
        document.set(model);
        RecordSchema schema = model == null ? null : model.schema().orElse(null);
        form.setSchema(schema);
        rebuildTable(schema);
        if (model != null) {
            model.addListener(documentListener);
        }
        rebuildTree();
        refreshAll();
        showRecord(null);
    }

    /**
     * Keeps the views in sync after a change: rebuilds the tree on structural changes, refreshes it on content
     * changes, and recomputes search results while keeping the current position.
     */
    private void onDocumentChanged(XmlDocumentModel model, DocumentChange change) {
        if (change != DocumentChange.SAVED) {
            // Matches may have changed: recompute, but stay at the same position in the result list.
            int position = searchCursor == null ? 0 : searchCursor.position();
            updateSearch(searchField.getText());
            if (searchCursor != null && position > 0 && !searchCursor.isEmpty()) {
                searchCursor.seek(position);
                searchCountLabel.setText(searchCursor.position() + " / " + searchCursor.size());
            }
        }
        switch (change) {
            case REPLACED, STRUCTURE -> rebuildTree();
            case CONTENT -> {
                documentTree.refresh();
                recordTable.refresh();
                // Re-show the selected record so the form never holds stale values (e.g. after an in-tree edit).
                showRecord(selectedRecord.get());
            }
            case SAVED -> { }
        }
        refreshAll();
    }

    /** Updates the table rows, status bar labels, XML preview and window title from the model. */
    private void refreshAll() {
        XmlDocumentModel model = document.get();
        if (model != null) {
            List<RecordNode> records = model.records();
            syncingSelection = true;
            recordTable.getItems().setAll(records);
            RecordNode selected = selectedRecord.get();
            if (selected != null && records.contains(selected)) {
                recordTable.getSelectionModel().select(selected);
            }
            syncingSelection = false;
            schemaLabel.setText(model.schema().map(s -> "Schema: " + s.displayName()).orElse("Generic XML"));
            countLabel.setText(model.schema().isPresent() ? records.size() + " record" + (records.size() == 1 ? "" : "s") : "");
            int encrypted = services.encryption().countEncrypted(model.document());
            encryptedLabel.setText(encrypted == 0 ? "" : encrypted + " encrypted node" + (encrypted == 1 ? "" : "s"));
            dirtyLabel.setText(model.isDirty() ? "● Unsaved changes" : "Saved");
            dirtyLabel.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("dirty"), model.isDirty());
        } else {
            recordTable.getItems().clear();
            schemaLabel.setText("");
            countLabel.setText("");
            encryptedLabel.setText("");
            dirtyLabel.setText("");
        }
        if (xmlTab.isSelected()) {
            refreshXmlPreview();
        }
        updateTitle();
    }

    /** Shows the current document as pretty-printed XML in the source tab. */
    private void refreshXmlPreview() {
        XmlDocumentModel model = document.get();
        xmlPreview.setText(model == null ? "" : XmlIO.toString(model.document()));
    }

    /** Shows the file name, with a dot when there are unsaved changes, in the window title. */
    private void updateTitle() {
        if (stage == null) {
            return;
        }
        XmlDocumentModel model = document.get();
        if (model == null) {
            stage.setTitle("DOM Parser");
        } else {
            stage.setTitle((model.isDirty() ? "● " : "") + documentName(model) + " — DOM Parser");
        }
    }

    /** The document's file name, or {@code "Untitled.xml"} if it has never been saved. */
    private static String documentName(XmlDocumentModel model) {
        return model.file().map(p -> p.getFileName().toString()).orElse("Untitled.xml");
    }

    // ---- tree -------------------------------------------------------------------------------------------------------

    /** Rebuilds the tree from the model, keeping the expanded nodes and the selection where they still exist. */
    private void rebuildTree() {
        XmlDocumentModel model = document.get();
        Element previouslySelected = selectedElement.get();
        Set<Element> expanded = Collections.newSetFromMap(new IdentityHashMap<>());
        treeIndex.forEach((element, item) -> {
            if (item.isExpanded()) {
                expanded.add(element);
            }
        });
        treeIndex.clear();
        if (model == null || !model.document().hasRootElement()) {
            documentTree.setRoot(null);
            treeSubtitle.setText("");
            return;
        }
        Element rootElement = model.document().getRootElement();
        TreeItem<Element> rootItem = buildItem(rootElement, expanded);
        rootItem.setExpanded(true);
        documentTree.setRoot(rootItem);
        treeSubtitle.setText(DocumentTreeCell.isEncrypted(rootElement)
                ? "The document is encrypted"
                : "Double-click a value to edit it");

        TreeItem<Element> reselect = previouslySelected == null ? null : treeIndex.get(previouslySelected);
        if (reselect != null) {
            documentTree.getSelectionModel().select(reselect);
        } else {
            documentTree.getSelectionModel().clearSelection();
            selectedElement.set(null);
        }
    }

    /** Creates the tree item for {@code element} and its children (encrypted data is shown as one leaf). */
    private TreeItem<Element> buildItem(Element element, Set<Element> expanded) {
        TreeItem<Element> item = new TreeItem<>(element);
        treeIndex.put(element, item);
        if (!XmlEncryptionService.isEncryptedData(element)) {
            for (Element child : element.getChildren()) {
                item.getChildren().add(buildItem(child, expanded));
            }
        }
        item.setExpanded(expanded.contains(element));
        return item;
    }

    /** Selects {@code element} in the tree, expanding its ancestors and scrolling it into view. */
    private void selectInTree(Element element) {
        TreeItem<Element> item = treeIndex.get(element);
        if (item == null) {
            return;
        }
        for (TreeItem<Element> p = item.getParent(); p != null; p = p.getParent()) {
            p.setExpanded(true);
        }
        documentTree.getSelectionModel().select(item);
        int row = documentTree.getRow(item);
        if (row >= 0) {
            documentTree.scrollTo(Math.max(0, row - 3));
        }
    }

    /** Callback for in-tree editing; shows the validation message and keeps the editor open if the value is invalid. */
    private boolean editElementText(Element element, String text) {
        try {
            document.get().setElementText(element, text);
            setStatus("Updated <" + element.getName() + ">");
            return true;
        } catch (ValidationException e) {
            setStatus(e.getMessage());
            dialogs.error("Invalid value", e.getMessage());
            return false;
        }
    }

    /** Expands every tree node. */
    @FXML
    private void onExpandAll() {
        treeIndex.values().forEach(i -> i.setExpanded(true));
    }

    /** Collapses the tree down to the root element's children. */
    @FXML
    private void onCollapseAll() {
        treeIndex.values().forEach(i -> i.setExpanded(false));
        if (documentTree.getRoot() != null) {
            documentTree.getRoot().setExpanded(true);
        }
    }

    // ---- table & form -------------------------------------------------------------------------------------------------

    /** Creates an id column plus one column per schema field; no columns for generic XML. */
    private void rebuildTable(RecordSchema schema) {
        recordTable.getColumns().clear();
        if (schema == null) {
            return;
        }
        TableColumn<RecordNode, String> idColumn = new TableColumn<>("#");
        idColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().id()));
        idColumn.setMinWidth(40);
        idColumn.setPrefWidth(48);
        idColumn.setMaxWidth(60);
        idColumn.getStyleClass().add("id-column");
        recordTable.getColumns().add(idColumn);
        for (FieldDefinition field : schema.fields()) {
            TableColumn<RecordNode, String> column = new TableColumn<>(field.label());
            column.setMinWidth(70);
            column.setPrefWidth(field.type() == com.aliibrahim.domparser.core.schema.FieldType.TEXT ? 140 : 80);
            column.setCellValueFactory(c -> {
                RecordNode record = c.getValue();
                String value = record.get(field.name());
                if (value.isEmpty() && record.element().getChild(field.name()) == null && hasEncryptedChild(record)) {
                    value = "🔒 encrypted";
                }
                return new ReadOnlyStringWrapper(value);
            });
            recordTable.getColumns().add(column);
        }
    }

    /** Whether any field of {@code record} is encrypted. */
    private static boolean hasEncryptedChild(RecordNode record) {
        return record.element().getChildren().stream().anyMatch(XmlEncryptionService::isEncryptedData);
    }

    /** Shows {@code record} in the form and selects it in the table; {@code null} shows an empty "new record" form. */
    private void showRecord(RecordNode record) {
        syncingSelection = true;
        if (record == null) {
            recordTable.getSelectionModel().clearSelection();
        } else {
            recordTable.getSelectionModel().select(record);
            recordTable.scrollTo(record);
        }
        syncingSelection = false;

        selectedRecordEncrypted.set(record != null && hasEncryptedChild(record));
        if (!form.hasSchema()) {
            formTitle.setText("Details");
            formSubtitle.setText(document.get() == null ? "" : "No schema for <"
                    + document.get().document().getRootElement().getName() + ">");
            return;
        }
        if (record == null) {
            form.clear();
            formTitle.setText("New record");
            formSubtitle.setText("Fill in the fields and click \"Add as new\"");
        } else {
            form.show(record.values());
            formTitle.setText(record.label());
            formSubtitle.setText(selectedRecordEncrypted.get()
                    ? "Contains encrypted fields — decrypt before editing"
                    : "Edit the fields and click \"Save changes\"");
        }
    }

    /** Clears the selection and starts an empty form for a new record. */
    @FXML
    private void onNewRecord() {
        documentTree.getSelectionModel().clearSelection();
        selectedElement.set(null);
        showRecord(null);
        form.focusFirst();
    }

    /** Adds the form values as a new record, or shows validation errors. */
    @FXML
    private void onAddRecord() {
        XmlDocumentModel model = document.get();
        if (model == null) {
            return;
        }
        try {
            RecordNode added = model.addRecord(form.values());
            selectInTree(added.element());
            setStatus("Added " + added.label());
        } catch (ValidationException e) {
            form.showErrors(e.errors());
            setStatus("Please fix the highlighted fields");
        }
    }

    /** Writes the form values into the selected record, or shows validation errors. */
    @FXML
    private void onApplyChanges() {
        XmlDocumentModel model = document.get();
        RecordNode record = selectedRecord.get();
        if (model == null || record == null) {
            return;
        }
        try {
            model.updateRecord(record, form.values());
            form.clearErrors();
            setStatus("Saved changes to " + record.label());
        } catch (ValidationException e) {
            form.showErrors(e.errors());
            setStatus("Please fix the highlighted fields");
        }
    }

    /** Deletes the selected record after confirmation. */
    @FXML
    private void onDeleteRecord() {
        XmlDocumentModel model = document.get();
        RecordNode record = selectedRecord.get();
        if (model == null || record == null) {
            return;
        }
        if (dialogs.confirm("Delete " + record.label() + "?", "This removes the record from the document.", "Delete")) {
            model.deleteRecord(record);
            setStatus("Deleted " + record.label());
        }
    }

    // ---- search -----------------------------------------------------------------------------------------------------

    /** Runs the search for {@code text} and updates the match count; blank text clears the search. */
    private void updateSearch(String text) {
        XmlDocumentModel model = document.get();
        if (model == null || text == null || text.isBlank()) {
            searchCursor = null;
            searchCountLabel.setText("");
            hasMatches.set(false);
            return;
        }
        searchCursor = services.search().search(model.records(), text);
        int n = searchCursor.size();
        hasMatches.set(n > 0);
        searchCountLabel.setText(n == 0 ? "No matches" : n + (n == 1 ? " match" : " matches"));
    }

    /** Focuses the search box and selects its text. */
    @FXML
    private void onFocusSearch() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    /** Jumps to the next match. */
    @FXML
    private void onSearchNext() {
        if (searchCursor == null) {
            updateSearch(searchField.getText());
        }
        if (searchCursor != null) {
            searchCursor.next().ifPresent(this::showMatch);
        }
    }

    /** Jumps to the previous match. */
    @FXML
    private void onSearchPrevious() {
        if (searchCursor == null) {
            updateSearch(searchField.getText());
        }
        if (searchCursor != null) {
            searchCursor.previous().ifPresent(this::showMatch);
        }
    }

    /** Selects the matching field in the tree and shows the position in the status bar. */
    private void showMatch(SearchMatch match) {
        Element fieldElement = match.record().element().getChild(match.field());
        selectInTree(fieldElement != null ? fieldElement : match.record().element());
        searchCountLabel.setText(searchCursor.position() + " / " + searchCursor.size());
        setStatus("Match in " + match.record().label() + " · " + match.record().schema().field(match.field()).label());
    }

    // ---- file actions -----------------------------------------------------------------------------------------------

    /** Creates a new empty document, asking for the document type when several schemas are registered. */
    @FXML
    private void onNewDocument() {
        if (!confirmDiscardChanges()) {
            return;
        }
        List<RecordSchema> schemas = services.schemas().all();
        Optional<RecordSchema> schema;
        if (schemas.size() == 1) {
            schema = Optional.of(schemas.get(0));
        } else {
            ChoiceDialog<RecordSchema> dialog = new ChoiceDialog<>(schemas.isEmpty() ? null : schemas.get(0), schemas);
            dialog.initOwner(stage);
            dialog.setTitle("New document");
            dialog.setHeaderText("What kind of document?");
            dialogs.style(dialog.getDialogPane());
            schema = dialog.showAndWait();
        }
        schema.ifPresent(s -> {
            setDocument(XmlDocumentModel.create(s));
            setStatus("New " + s.displayName() + " document");
            form.focusFirst();
        });
    }

    /** Asks for an XML file and opens it, after handling unsaved changes. */
    @FXML
    private void onOpen() {
        if (!confirmDiscardChanges()) {
            return;
        }
        FileChooser chooser = xmlChooser("Open XML file");
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            open(file.toPath());
        }
    }

    /**
     * Opens {@code path} in the background. Does not check for unsaved changes; callers do that first.
     *
     * @param path the XML file to open
     */
    public void open(Path path) {
        services.preferences().put(PREF_LAST_DIR, path.toAbsolutePath().getParent().toString());
        runTask("Opening " + path.getFileName(), () -> XmlDocumentModel.open(path, services.schemas()), model -> {
            setDocument(model);
            setStatus("Opened " + path.getFileName()
                    + model.schema().map(s -> " · " + model.records().size() + " " + s.displayName().toLowerCase()).orElse(""));
        });
    }

    /** Saves to the current file (asks for one if the document is new). */
    @FXML
    private void onSave() {
        saveDocument(false);
    }

    /** Asks for a file and saves the document there. */
    @FXML
    private void onSaveAs() {
        saveDocument(true);
    }

    /**
     * Saves the document, asking for a file when {@code chooseFile} is true or the document is new.
     *
     * @return true when the document was saved
     */
    private boolean saveDocument(boolean chooseFile) {
        XmlDocumentModel model = document.get();
        if (model == null) {
            return false;
        }
        Path target = model.file().orElse(null);
        if (chooseFile || target == null) {
            FileChooser chooser = xmlChooser("Save XML file");
            chooser.setInitialFileName(documentName(model));
            File file = chooser.showSaveDialog(stage);
            if (file == null) {
                return false;
            }
            target = file.toPath();
            services.preferences().put(PREF_LAST_DIR, target.toAbsolutePath().getParent().toString());
        }
        try {
            model.saveAs(target);
            setStatus("Saved " + target.getFileName());
            return true;
        } catch (Exception e) {
            dialogs.error("Could not save " + target.getFileName(), e);
            return false;
        }
    }

    /** A file chooser for XML files that starts in the last used directory. */
    private FileChooser xmlChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML files", "*.xml"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        Path dir = Path.of(services.preferences().get(PREF_LAST_DIR, System.getProperty("user.dir")));
        if (Files.isDirectory(dir)) {
            chooser.setInitialDirectory(dir.toFile());
        }
        return chooser;
    }

    /** @return true when it is fine to drop the current document. */
    private boolean confirmDiscardChanges() {
        XmlDocumentModel model = document.get();
        if (model == null || !model.isDirty()) {
            return true;
        }
        return switch (dialogs.unsavedChanges(documentName(model))) {
            case SAVE -> saveDocument(false);
            case DISCARD -> true;
            case CANCEL -> false;
        };
    }

    /** Closes the window after handling unsaved changes. */
    @FXML
    private void onExit() {
        if (confirmDiscardChanges()) {
            stage.setOnCloseRequest(null);
            stage.close();
        }
    }

    /** Opens a file dropped anywhere on the window, after handling unsaved changes. */
    private void installDragAndDrop() {
        root.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        root.setOnDragDropped(e -> {
            List<File> files = e.getDragboard().getFiles();
            boolean ok = files != null && !files.isEmpty();
            e.setDropCompleted(ok);
            e.consume();
            if (ok) {
                // Leave the drag-and-drop gesture before a modal "unsaved changes" dialog can open.
                Path dropped = files.get(0).toPath();
                Platform.runLater(() -> {
                    if (confirmDiscardChanges()) {
                        open(dropped);
                    }
                });
            }
        });
    }

    // ---- view -------------------------------------------------------------------------------------------------------

    /** Switches between light and dark theme and remembers the choice. */
    @FXML
    private void onToggleTheme() {
        services.preferences().putBoolean(PREF_DARK, darkThemeMenuItem.isSelected());
        applyTheme();
    }

    /** Applies the theme selected in the View menu. */
    private void applyTheme() {
        root.getStyleClass().remove("dark");
        if (darkThemeMenuItem.isSelected()) {
            root.getStyleClass().add("dark");
        }
    }

    /** Shows the About dialog. */
    @FXML
    private void onAbout() {
        dialogs.info("DOM Parser 2.0",
                "Browse XML documents as a DOM tree, edit records through schema-driven forms, search them, "
                        + "and protect nodes with W3C XML Encryption.\n\nAuthor: Ali Ibrahim");
    }

    // ---- FeatureContext -----------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public AppServices services() {
        return services;
    }

    /** {@inheritDoc} */
    @Override
    public ReadOnlyObjectProperty<XmlDocumentModel> documentProperty() {
        return document.getReadOnlyProperty();
    }

    /** {@inheritDoc} */
    @Override
    public ReadOnlyObjectProperty<Element> selectedElementProperty() {
        return selectedElement.getReadOnlyProperty();
    }

    /** {@inheritDoc} */
    @Override
    public ReadOnlyBooleanProperty busyProperty() {
        return busy.getReadOnlyProperty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Menus are matched by name, ignoring case and mnemonic underscores.
     */
    @Override
    public void addMenuItem(String menuName, MenuItem item) {
        Menu menu = menuBar.getMenus().stream()
                .filter(m -> m.getText().replace("_", "").equalsIgnoreCase(menuName))
                .findFirst()
                .orElseGet(() -> {
                    Menu created = new Menu(menuName);
                    menuBar.getMenus().add(menuBar.getMenus().indexOf(helpMenu), created);
                    return created;
                });
        menu.getItems().add(item);
    }

    /** {@inheritDoc} */
    @Override
    public void addToolbarItem(Node node) {
        toolBar.getItems().add(toolBar.getItems().indexOf(toolbarSpacer), node);
    }

    /** {@inheritDoc} */
    @Override
    public Dialogs dialogs() {
        return dialogs;
    }

    /** {@inheritDoc} */
    @Override
    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Tasks run one at a time on the shared background thread, and {@link #busyProperty()} is true meanwhile.
     */
    @Override
    public <T> void runTask(String description, Callable<T> work, Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> {
            busy.set(false);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            busy.set(false);
            setStatus(description + " failed");
            dialogs.error(description + " failed", task.getException());
        });
        busy.set(true);
        setStatus(description + "…");
        services.background().execute(task);
    }
}
