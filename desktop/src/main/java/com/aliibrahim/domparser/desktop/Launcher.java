package com.aliibrahim.domparser.desktop;

import javafx.application.Application;

/**
 * Plain entry point. Keeping {@code main} out of the {@link Application} subclass lets the app start from a regular
 * classpath (fat jar, IDE run configuration) without the JavaFX launcher complaining about missing modules.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(DomParserApp.class, args);
    }
}
