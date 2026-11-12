package com.aliibrahim.domparser.desktop.ui;

import javafx.scene.layout.Region;

/** Vector icons (Material Design paths, Apache 2.0) rendered as CSS shapes so they follow the theme colour. */
public enum Icon {
    /** A new document. */
    FILE("M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13z"),
    /** An open folder. */
    OPEN("M20 6h-8l-2-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm0 12H4V8h16v10z"),
    /** A floppy disk. */
    SAVE("M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 "
            + "3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"),
    /** A plus sign. */
    ADD("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"),
    /** A rubbish bin. */
    DELETE("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"),
    /** A closed padlock. */
    LOCK("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1"
            + "-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 "
            + "0 3.1 1.39 3.1 3.1v2z"),
    /** An open padlock. */
    UNLOCK("M12 17c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm6-9h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6h1.9c0-1.71 "
            + "1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0"
            + "-1.1-.9-2-2-2zm0 12H6V10h12v10z"),
    /** A magnifying glass. */
    SEARCH("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61"
            + " 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01"
            + " 14 9.5 11.99 14 9.5 14z"),
    /** An upward chevron. */
    UP("M7.41 15.41L12 10.83l4.59 4.58L18 14l-6-6-6 6z"),
    /** A downward chevron. */
    DOWN("M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z");

    private final String path;

    Icon(String path) {
        this.path = path;
    }

    /**
     * Creates a new node showing this icon. Each call returns a new node, because a JavaFX node can only
     * appear in one place.
     *
     * @return a region shaped like the icon, styled with {@code .icon} and {@code .icon-<name>}
     */
    public Region node() {
        Region region = new Region();
        region.getStyleClass().addAll("icon", "icon-" + name().toLowerCase());
        region.setStyle("-fx-shape: \"" + path + "\";");
        return region;
    }
}
