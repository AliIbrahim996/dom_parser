<h1 align="center">XML DOM Parser</h1>

A JavaFX desktop app for exploring and editing XML documents as a DOM tree: edit records through schema-driven
forms, search them, and protect nodes with W3C XML Encryption.

## Features

- **Document tree.** Any XML file opens as a tree showing tags, attributes and values. Double-click a value to edit it in place.
- **Records table and form.** For known document types (schemas), records appear in a table, with an edit form
  built from the schema. Values are validated as you edit.
- **Search.** A case-insensitive search across all fields, with next/previous (`Enter`, `F3`, `Shift+F3`).
- **XML Encryption.** Encrypt a field, a record or the whole document (AES-256-GCM, with keys wrapped by AES-256 KW).
  You can decrypt again during the same session.
- **XML source view**, **light/dark theme**, drag-and-drop to open, and a prompt about unsaved changes when you close.

## Requirements

- JDK 21 or newer. Gradle downloads JavaFX and everything else, and the Gradle wrapper is included.

## Build & run

```bash
./gradlew run          # start the app (the open dialog starts in samples/)
./gradlew build        # compile + run all tests
./gradlew installDist  # self-contained app in desktop/build/install/dom-parser
```

On Windows use `gradlew.bat`. You can pass a file to open: `./gradlew run --args="users.xml"`.

`./gradlew javadoc` builds the API docs and fails if a public class or method is undocumented.

### Continuous integration

[`.github/workflows/build.yml`](.github/workflows/build.yml) runs on every pull request, on pushes to `main` and on
`v*` tags. It builds, tests and checks the docs on Linux, Windows and macOS, then uploads a ready-to-run zip per
platform (`dom-parser-linux`, `dom-parser-windows`, `dom-parser-macos`) as workflow artifacts. JavaFX includes
native libraries, so use the zip for your OS. Unzip it and run `bin/dom-parser` (or `bin\dom-parser.bat` on Windows).
A JDK or JRE 21+ must be installed.

## Project structure

```
dom_parser/
├── core/                         UI-independent library (no JavaFX)
│   └── com.aliibrahim.domparser.core
│       ├── schema/               RecordSchema, FieldDefinition, SchemaRegistry, UserSchema
│       ├── document/             XmlDocumentModel (single source of truth), RecordNode, XmlIO, DocumentConverter
│       ├── search/               SearchService, SearchCursor
│       └── security/             EncryptionService, XmlEncryptionService (Apache Santuario)
├── desktop/                      JavaFX client
│   └── com.aliibrahim.domparser.desktop
│       ├── DomParserApp, AppServices   app bootstrap and shared services
│       ├── feature/              Feature plug-in API + EncryptionFeature
│       └── ui/                   MainController, RecordForm, DocumentTreeCell, Dialogs, Icon
│       resources/…/main-view.fxml, app.css
├── samples/users.xml
└── gradle/libs.versions.toml     all dependency versions in one place
```

**Rules that keep it flexible**

- `core` has no UI code, so you can test it on its own (`./gradlew :core:test`) and reuse it in a CLI or web front end.
- Every change to a document goes through `XmlDocumentModel`, which notifies `DocumentListener`s. The UI never
  edits the XML tree directly.
- The UI is generated from `RecordSchema`. Nothing in the desktop module hard-codes "User".

## Extending

### Support a new XML document type

Implement `RecordSchema` and register it. The table, form, validation and search adapt automatically.

```java
public final class BookSchema implements RecordSchema {
    public String id() { return "books"; }
    public String displayName() { return "Books"; }
    public String rootElement() { return "Library"; }
    public String recordElement() { return "Book"; }
    public List<FieldDefinition> fields() {
        return List.of(
                FieldDefinition.text("title", "Title"),
                FieldDefinition.text("author", "Author"),
                FieldDefinition.integer("year", "Year").optional(),
                FieldDefinition.choice("format", "Format", "Hardcover", "Paperback", "E-book"));
    }
}
```

Add its class name on a new line in
`core/src/main/resources/META-INF/services/com.aliibrahim.domparser.core.schema.RecordSchema`.

### Add a new feature (menu, toolbar, behaviour)

Implement `Feature` and register it. The `FeatureContext` gives the feature access to the open document, the
current selection, background tasks, dialogs, menus and the toolbar.

```java
public final class WordCountFeature implements Feature {
    public String id() { return "word-count"; }
    public void install(FeatureContext ctx) {
        MenuItem item = new MenuItem("Count records");
        item.disableProperty().bind(ctx.documentProperty().isNull());
        item.setOnAction(e -> ctx.dialogs().info("Records",
                ctx.documentProperty().get().records().size() + " records"));
        ctx.addMenuItem("Tools", item);   // the "Tools" menu is created on demand
    }
}
```

Add its class name on a new line in
`desktop/src/main/resources/META-INF/services/com.aliibrahim.domparser.desktop.feature.Feature`.
For a complete example, see `EncryptionFeature`.

### Swap a service

`AppServices.createDefault()` is the single place where services are created. For example, you can supply a
different `EncryptionService` there, or add a new service for features to use.

## Notes

- The encryption key exists only in memory. Encrypted content you save can't be decrypted after the app restarts.
  This is deliberate for now. Storing keys would be a separate feature.
- For safety, parsing rejects `DOCTYPE` declarations (this prevents XXE attacks).

## Contact

#### [Ali Ibrahim](mailto:ali.ibrahi.2@lit-co.net)
#### [LinkedIn](https://www.linkedin.com/in/ali-ibrahim-b978b4181/)
