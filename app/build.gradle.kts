plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.danilopianini.gradle-java-qa") version "1.155.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
}

dependencies {
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jooq:jool:0.9.15")

    runtimeOnly("ch.qos.logback:logback-classic:1.5.21")

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Utilities
    implementation(libs.guava)
}

application {
    mainClass.set("it.unibo.unibodget.Unibodget")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}