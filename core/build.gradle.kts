plugins {
    `java-library`
}

description = "UI-independent XML document model, schemas, search and encryption services."

dependencies {
    api(libs.jdom2)
    implementation(libs.xmlsec)
    testRuntimeOnly(libs.slf4j.simple)
}
