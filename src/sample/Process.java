package sample;

import DOMParser.DOMXmlParser;
import DOMParser.Parser;
import Model.User;
import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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

    public void save_file(String file_name) throws InterruptedException {
        Thread.sleep(2000);
        synchronized (this) {
            try {
                Document doc = new Document();
                doc.setRootElement(new Element("Users"));

                for (User user : parser.getUserList()) {
                    doc.getRootElement().addContent(createUserXMLElement(
                            user.getId() + "", user.getFirstName(), user.getLastName(), user.getAge() + "", user.getGender()));
                }

                // new XMLOutputter().output(doc, System.out);
                XMLOutputter xmlOutput = new XMLOutputter();
                // xmlOutput.output(doc, System.out);
                // display nice nice
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter("create_jdom_users.xml"));
                write_to_text_area("\n** File " + file_name + " saved successfully!**\n");
                notify();
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static Element createUserXMLElement(String id, String firstName, String lastName, String age,
                                                String gender) {
        Element user = new Element("User");
        user.setAttribute(new Attribute("id", id));
        user.addContent(new Element("firstName").setText(firstName));
        user.addContent(new Element("lastName").setText(lastName));
        user.addContent(new Element("age").setText(age));
        user.addContent(new Element("gender").setText(gender));
        return user;
    }
}