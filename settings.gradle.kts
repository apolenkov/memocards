pluginManagement {
    val sonarlint = providers.gradleProperty("sonarlint").get()
    val dependencycheck = providers.gradleProperty("dependencycheck").get()
    val spotbugsPluginVersion = providers.gradleProperty("spotbugsPluginVersion").get()
    val vaadinVersion = providers.gradleProperty("vaadinVersion").get()
    val dependencyManagement = providers.gradleProperty("dependencyManagement").get()
    val springframeworkBoot = providers.gradleProperty("springframeworkBoot").get()
    val spotless = providers.gradleProperty("spotless").get()
    val nodeGradlePlugin = providers.gradleProperty("nodeGradlePlugin").get()
    val jib = providers.gradleProperty("jib").get()

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.diffplug.spotless") version spotless
        id("name.remal.sonarlint") version sonarlint
        id("com.github.spotbugs") version spotbugsPluginVersion

        id("org.owasp.dependencycheck") version dependencycheck
        id("io.spring.dependency-management") version dependencyManagement

        id("com.vaadin") version vaadinVersion
        id("com.github.node-gradle.node") version nodeGradlePlugin
        id("org.springframework.boot") version springframeworkBoot

        id("com.google.cloud.tools.jib") version jib
    }
}

rootProject.name = "memocards"
