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

    /** Short unique id, used in logs and preferences. */
    String id();

    void install(FeatureContext context);
}
