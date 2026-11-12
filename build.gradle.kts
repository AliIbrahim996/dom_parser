plugins {
    alias(libs.plugins.javafx) apply false
}

allprojects {
    group = "com.aliibrahim.domparser"
    version = "2.0.0"
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
        "testImplementation"(libs.findLibrary("junit-jupiter").get())
        "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Compile for Java 21 with whichever JDK (21 or newer) runs Gradle; no exact toolchain needed.
        options.release.set(21)
        options.compilerArgs.add("-Xlint:all,-serial,-processing")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
