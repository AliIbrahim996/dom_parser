plugins {
    application
    alias(libs.plugins.javafx)
}

description = "JavaFX desktop client for DOM Parser."

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":core"))
    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("com.aliibrahim.domparser.desktop.Launcher")
    applicationName = "dom-parser"
}

distributions {
    main {
        // Ship the sample document with the app.
        contents {
            from(rootProject.file("samples")) { into("samples") }
        }
    }
}

tasks.named<JavaExec>("run") {
    // Start in the samples folder so the open dialog lands next to users.xml.
    workingDir = rootProject.file("samples")
}
