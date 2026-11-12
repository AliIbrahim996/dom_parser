package com.aliibrahim.domparser.desktop;

import com.aliibrahim.domparser.core.schema.SchemaRegistry;
import com.aliibrahim.domparser.core.search.SearchService;
import com.aliibrahim.domparser.core.security.EncryptionService;
import com.aliibrahim.domparser.core.security.XmlEncryptionService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * The application's shared services, created once at start-up and handed to the controller and every feature.
 * This is the one place to swap an implementation (e.g. a different {@link EncryptionService}) or add a new service.
 */
public record AppServices(
        SchemaRegistry schemas,
        EncryptionService encryption,
        SearchService search,
        ExecutorService background,
        Preferences preferences) {

    public static AppServices createDefault() {
        ExecutorService background = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "dom-parser-worker");
            t.setDaemon(true);
            return t;
        });
        return new AppServices(
                SchemaRegistry.discover(),
                new XmlEncryptionService(),
                new SearchService(),
                background,
                Preferences.userNodeForPackage(AppServices.class));
    }
}
