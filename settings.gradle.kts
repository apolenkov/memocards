pluginManagement {
    val jgitver: String by settings
    val sonarlint: String by settings
    val dependencycheck: String by settings
    val spotbugsPluginVersion: String by settings
    val vaadinVersion: String by settings
    val dependencyManagement: String by settings
    val springframeworkBoot: String by settings
    val spotless: String by settings
    val nodeVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("fr.brouillard.oss.gradle.jgitver") version jgitver

        id("com.diffplug.spotless") version spotless
        id("name.remal.sonarlint") version sonarlint
        id("com.github.spotbugs") version spotbugsPluginVersion

        id("org.owasp.dependencycheck") version dependencycheck
        id("io.spring.dependency-management") version dependencyManagement

        id("com.vaadin") version vaadinVersion
        id("com.github.node-gradle.node") version nodeVersion
        id("org.springframework.boot") version springframeworkBoot
    }
}

rootProject.name = "memocards"
