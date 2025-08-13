pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        val vaadinVersion: String by settings
        id("com.vaadin") version vaadinVersion
    }
}

rootProject.name = "flashcards"


