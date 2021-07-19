package sample;

import javafx.application.Platform;
class Process {

    public Controller controller;

    public Process(Controller c) {
        this.controller = c;
    }

    public void process() throws InterruptedException {
        synchronized (this) {
            wait();
            write_to_text_area("\nProgram Resumed!..\n");
        }
    }

    public void write_to_text_area(String s) {
        Platform.runLater(() -> controller._text.appendText(s));
    }

}