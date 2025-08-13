pluginManagement {
    val jgitver: String by settings
    val sonarlint: String by settings
    val dependencycheck: String by settings
    val spotbugsPluginVersion: String by settings
    val vaadinVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("fr.brouillard.oss.gradle.jgitver") version jgitver
        id("name.remal.sonarlint") version sonarlint
        id("org.owasp.dependencycheck") version dependencycheck
        id("com.github.spotbugs") version spotbugsPluginVersion
        id("com.vaadin") version vaadinVersion
    }
}

rootProject.name = "flashcards"
