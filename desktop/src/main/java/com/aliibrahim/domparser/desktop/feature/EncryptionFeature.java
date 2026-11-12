package com.aliibrahim.domparser.desktop.feature;

import com.aliibrahim.domparser.core.document.XmlDocumentModel;
import com.aliibrahim.domparser.core.security.EncryptionService;
import com.aliibrahim.domparser.core.security.XmlEncryptionService;
import com.aliibrahim.domparser.desktop.ui.Icon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import org.jdom2.Document;
import org.jdom2.Element;

/** "Security" menu: XML Encryption of a field, a record or the whole document, and decryption within the session. */
public final class EncryptionFeature implements Feature {

    /** Created by {@link java.util.ServiceLoader}. */
    public EncryptionFeature() {
    }

    /** Returns {@code "encryption"}. */
    @Override
    public String id() {
        return "encryption";
    }

    /** Adds the Security menu items and the Encrypt toolbar button, enabled only when they apply. */
    @Override
    public void install(FeatureContext ctx) {
        EncryptionService encryption = ctx.services().encryption();

        BooleanBinding noDocument = ctx.documentProperty().isNull().or(ctx.busyProperty());
        BooleanBinding nothingToEncrypt = Bindings.createBooleanBinding(
                () -> ctx.documentProperty().get() == null
                        || ctx.selectedElementProperty().get() == null
                        || XmlEncryptionService.isWithinEncryptedData(ctx.selectedElementProperty().get()),
                ctx.documentProperty(), ctx.selectedElementProperty()).or(ctx.busyProperty());

        MenuItem encryptSelection = new MenuItem("Encrypt selected node", Icon.LOCK.node());
        encryptSelection.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        encryptSelection.disableProperty().bind(nothingToEncrypt);
        encryptSelection.setOnAction(e -> encryptSelected(ctx, encryption));

        MenuItem encryptDocument = new MenuItem("Encrypt whole document");
        encryptDocument.disableProperty().bind(noDocument);
        encryptDocument.setOnAction(e -> encryptDocument(ctx, encryption));

        MenuItem decrypt = new MenuItem("Decrypt all (this session)", Icon.UNLOCK.node());
        decrypt.disableProperty().bind(noDocument);
        decrypt.setOnAction(e -> decryptAll(ctx, encryption));

        ctx.addMenuItem("Security", encryptSelection);
        ctx.addMenuItem("Security", encryptDocument);
        ctx.addMenuItem("Security", new SeparatorMenuItem());
        ctx.addMenuItem("Security", decrypt);

        Button button = new Button("Encrypt", Icon.LOCK.node());
        button.getStyleClass().add("tool-button");
        button.setTooltip(new Tooltip("Encrypt the selected node (Ctrl+E)"));
        button.disableProperty().bind(nothingToEncrypt);
        button.setOnAction(e -> encryptSelected(ctx, encryption));
        ctx.addToolbarItem(button);
    }

    /** Encrypts the selected tree element after confirmation (whole document when the root is selected). */
    private void encryptSelected(FeatureContext ctx, EncryptionService encryption) {
        XmlDocumentModel model = ctx.documentProperty().get();
        Element target = ctx.selectedElementProperty().get();
        if (model == null || target == null) {
            return;
        }
        if (target == model.document().getRootElement()) {
            encryptDocument(ctx, encryption);
            return;
        }
        String what = model.recordOf(target)
                .map(r -> r.element() == target ? r.label() : "\"" + target.getName() + "\" of " + r.label())
                .orElse("<" + target.getName() + ">");
        if (!ctx.dialogs().confirm("Encrypt " + what + "?",
                "The node will be replaced by an <EncryptedData> element. It can be decrypted again only "
                        + "until you close the application, because the key is not stored.",
                "Encrypt")) {
            return;
        }
        Document source = model.document();
        ctx.runTask("Encrypting " + what, () -> encryption.encryptElement(source, target), encrypted -> {
            model.replaceDocument(encrypted);
            ctx.setStatus(what + " encrypted");
        });
    }

    /** Encrypts the whole document after confirmation. */
    private void encryptDocument(FeatureContext ctx, EncryptionService encryption) {
        XmlDocumentModel model = ctx.documentProperty().get();
        if (model == null || !ctx.dialogs().confirm("Encrypt the whole document?",
                "Every element will be replaced by a single <EncryptedData> element. It can be decrypted again "
                        + "only until you close the application.",
                "Encrypt document")) {
            return;
        }
        Document source = model.document();
        ctx.runTask("Encrypting document", () -> encryption.encryptDocument(source), encrypted -> {
            model.replaceDocument(encrypted);
            ctx.setStatus("Document encrypted");
        });
    }

    /** Decrypts every node encrypted in this session; tells the user when there is nothing to decrypt. */
    private void decryptAll(FeatureContext ctx, EncryptionService encryption) {
        XmlDocumentModel model = ctx.documentProperty().get();
        if (model == null) {
            return;
        }
        if (encryption.countEncrypted(model.document()) == 0) {
            ctx.dialogs().info("Nothing to decrypt", "This document contains no encrypted nodes.");
            return;
        }
        Document source = model.document();
        ctx.runTask("Decrypting", () -> encryption.decryptAll(source), decrypted -> {
            model.replaceDocument(decrypted);
            ctx.setStatus("All encrypted nodes decrypted");
        });
    }
}
