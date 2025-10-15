plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.1.0" //version mas reciente
    id("org.beryx.jlink") version "2.26.0" //version mas reciente
}

group = "tecnm.celaya.edu.mx"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("tecnm.celaya.edu.mx.despachadorapp")
    mainClass.set("tecnm.celaya.edu.mx.despachadorapp.HelloApplication")
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// --- Configuración para crear el Instalador Nativo ---
jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "DespachadorApp"
    }
    jpackage {
        // El nombre del instalador se genera automáticamente a partir del nombre y versión del proyecto.
        installerType = "exe"
        installerOptions.addAll(listOf(
            "--win-shortcut",
            "--win-menu",
            "--win-dir-chooser"
        ))
    }
}
