import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

// import name.remal.gradle_plugins.sonarlint.SonarLintExtension

val javaTargetVersion = JavaVersion.VERSION_21

/*
 * Top-level plugins configuration
 */
plugins {
    idea
    id("fr.brouillard.oss.gradle.jgitver")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    id("com.vaadin")
    id("com.diffplug.spotless") version "6.25.0"
//    id("name.remal.sonarlint")
    id("org.owasp.dependencycheck")
//    id("checkstyle")
    id("jacoco")
//    id("com.github.spotbugs")
}

group = "org.apolenkov.application"
version = "1.0-SNAPSHOT"
description = "flashcards"

/*
 * IDE configuration
 */
idea {
    project {
        languageLevel = IdeaLanguageLevel(javaTargetVersion)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    sourceCompatibility = javaTargetVersion
    targetCompatibility = javaTargetVersion
}

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
    maven(url = "https://maven.vaadin.com/vaadin-addons")
}

/*
 * Enhanced dependency management with BOMs and explicit versions
 */
apply(plugin = "io.spring.dependency-management")
extensions.configure<DependencyManagementExtension> {
    dependencies {
        imports {
            val vaadinVersion: String by project
            mavenBom(BOM_COORDINATES)
            mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
            mavenBom("org.testcontainers:testcontainers-bom:1.20.1")
        }

        // Explicit dependency versions for better control
        val asm: String by project
        dependency("org.ow2.asm:asm-commons:$asm")
        dependency("com.google.guava:guava:33.0.0-jre")
        dependency("org.apache.commons:commons-lang3:3.14.0")
    }
}

dependencies {
    // Utilities
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")

    // Vaadin
    implementation("com.vaadin:vaadin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.parttio:line-awesome:2.1.0")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("com.vaadin:vaadin-testbench-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

// Default task
defaultTasks("bootRun")

/*
 * Enhanced Spotless configuration following example patterns
 */
apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("src/**/*.java")
        targetExclude("**/build/**/*.java")

        importOrder(
            "java",
            "javax",
            "jakarta",
            "org",
            "com",
            "org.apolenkov",
            "",
        ).wildcardsLast()
        removeUnusedImports()

        cleanthat()
            .sourceCompatibility(javaTargetVersion.toString())

        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()

        palantirJavaFormat("2.63.0")
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktlint()
        endWithNewline()
        trimTrailingWhitespace()
    }
}

/*
 * Enhanced Checkstyle configuration
 */

// apply(plugin = "checkstyle")
// checkstyle {
//    toolVersion = "10.12.1"
//    configFile = file("config/checkstyle/checkstyle.xml")
//    isShowViolations = true
// }

/*
 * SpotBugs configuration
 */

// apply(plugin = "com.github.spotbugs")
// tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
//    showProgress = true
//    showStackTraces = true
//
//    includeFilter = file("config/spotbugs/include.xml")
//
//    reports {
//        create("html") {
//            required.set(true)
//        }
//    }
// }

/*
 * SonarLint configuration
 */

// apply<name.remal.gradle_plugins.sonarlint.SonarLintPlugin>()
// extensions.configure<SonarLintExtension> {
//    languages { include("java") }
// }

/*
 * OWASP Dependency Check configuration
 */
apply(plugin = "org.owasp.dependencycheck")

/*
 * JGitver automatic versioning
 */
plugins.apply(fr.brouillard.oss.gradle.plugins.JGitverPlugin::class.java)
extensions.configure<fr.brouillard.oss.gradle.plugins.JGitverPluginExtension> {
    strategy("PATTERN")
    nonQualifierBranches("main,master")
    tagVersionPattern("\${v}\${<meta.DIRTY_TEXT}")
    versionPattern(
        "\${v}\${<meta.COMMIT_DISTANCE}\${<meta.GIT_SHA1_8}" +
            "\${<meta.QUALIFIED_BRANCH_NAME}\${<meta.DIRTY_TEXT}-SNAPSHOT",
    )
}

/*
 * Enhanced JaCoCo configuration with coverage verification
 */
apply(plugin = "jacoco")
jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test> {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Run tests"

    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    finalizedBy(tasks.named("jacocoTestCoverageVerification"))
}

/*
 * Compilation configuration
 */
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Xlint:all,-serial,-processing",
        ),
    )
    dependsOn("spotlessApply")
}

/*
 * Enhanced check task with all quality gates
 */
tasks.named("check") {
    description = "Run all verification tasks including formatting, static analysis, and tests"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "spotlessCheck",
//        "sonarlintMain",
//        "sonarlintTest",
//        "checkstyleMain",
//        "checkstyleTest",
//        "spotbugsMain",
//        "spotbugsTest",
//        "dependencyCheckAnalyze",
        "jacocoTestCoverageVerification",
    )
}

/*
 * Configuration ordering
 */
tasks.named("spotlessJava") {
    dependsOn("vaadinPrepareFrontend")
}

/*
 * Resolution strategy for dependency conflicts
 */
configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

        // Force commons-io version to resolve conflicts with SonarLint
        force("commons-io:commons-io:2.19.0")

        // Force ASM version to resolve conflicts between JaCoCo and SpotBugs
        val asm: String by project
        force("org.ow2.asm:asm:$asm")
        force("org.ow2.asm:asm-tree:$asm")
        force("org.ow2.asm:asm-analysis:$asm")
        force("org.ow2.asm:asm-util:$asm")

        // Force checker-qual version to resolve Spotless conflicts
        force("org.checkerframework:checker-qual:3.45.0")

        // Force commons-compress version to resolve test classpath conflicts
        force("org.apache.commons:commons-compress:1.27.1")

        // Force jetbrains annotations version to resolve test classpath conflicts
        force("org.jetbrains:annotations:17.0.0")

        // Force classgraph version to resolve test classpath conflicts
        force("io.github.classgraph:classgraph:4.8.179")
    }
}

/*
 * Utility task to show managed versions
 */
tasks.register("managedVersions") {
    description = "Prints all managed dependency versions"
    group = JavaBasePlugin.VERIFICATION_GROUP

    doLast {
        project.extensions
            .getByType<DependencyManagementExtension>()
            .managedVersions
            .toSortedMap()
            .map { "${it.key}:${it.value}" }
            .forEach(::println)
    }
}
