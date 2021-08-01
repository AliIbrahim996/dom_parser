package sample;

import DOMParser.Parser;
import javafx.application.Platform;
import java.io.File;
import javax.xml.transform.TransformerException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Process {

    public Controller controller;

    public Process(Controller c) {
        this.controller = c;
    }

    Parser parser;

    public void setParser(Parser parser) {
        this.parser = parser;
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

    public void save_file(String file_name,Parser parser) throws InterruptedException {
        Thread.sleep(2000);
        synchronized (this) {
            try {
                parser.write_xml_file(new File(file_name));
                write_to_text_area("\n** File " + file_name + " saved successfully!**\n");
                notify();
            } catch ( TransformerException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}