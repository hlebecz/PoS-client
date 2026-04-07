plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    checkstyle
    id("com.diffplug.spotless") version "8.4.0"
}

group = "kurs.client"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

javafx {
    version = "26"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

checkstyle {
    toolVersion = "10.18.2"
    configFile = rootProject.file("config/checkstyle/google_checks.xml")
}

spotless {
    java {
        googleJavaFormat("1.35.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        importOrder("java", "javax", "org", "com", "lombok")
        targetExclude("build/**")
    }

    format("gradle") {
        target("**/*.gradle", "**/*.gradle.kts")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }

    format("misc") {
        target("**/*.md", "**/*.properties", "**/*.yml", "**/*.yaml")
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("kurs.client.ClientApp")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.build {
    dependsOn(tasks.named("spotlessApply"))
}
