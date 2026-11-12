package com.aliibrahim.domparser.desktop.feature;

/**
 * A pluggable piece of functionality: it receives a {@link FeatureContext} once at start-up and contributes menu
 * items, toolbar buttons and behaviour through it.
 *
 * <p>To add a feature: implement this interface (public no-arg constructor) and list the class in
 * {@code desktop/src/main/resources/META-INF/services/com.aliibrahim.domparser.desktop.feature.Feature}.
 * See {@link EncryptionFeature} for a complete example.
 */
public interface Feature {

    /**
     * A short unique id, used in logs and preferences.
     *
     * @return the feature id
     */
    String id();

    /**
     * Called once on the UI thread at start-up. Add menu items, toolbar items and listeners here.
     *
     * @param context access to the main window and the shared services
     */
    void install(FeatureContext context);
}
