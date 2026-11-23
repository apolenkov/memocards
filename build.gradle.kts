import groovy.json.JsonSlurper
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import name.remal.gradle_plugins.sonarlint.SonarLintExtension
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
import java.util.zip.ZipFile

val javaTargetVersion = JavaVersion.VERSION_21
val vaadinVersion = providers.gradleProperty("vaadinVersion").get()
val assertj = providers.gradleProperty("assertj").get()
val mockito = providers.gradleProperty("mockito").get()
val springDotenv = providers.gradleProperty("springDotenv").get()
val testcontainersVersion = providers.gradleProperty("testcontainersVersion").get()

plugins {
    idea
    id("java")

    id("org.springframework.boot")
    id("com.github.node-gradle.node")
    id("com.vaadin")

    id("io.spring.dependency-management")
    id("org.owasp.dependencycheck")

    id("com.diffplug.spotless")
    id("name.remal.sonarlint")
    id("com.github.spotbugs")
    id("checkstyle")
    id("pmd")

    id("jacoco")

    id("com.google.cloud.tools.jib")
}

group = "org.apolenkov.application"
// Use the release tag on tagged commits and include distance and SHA on snapshots.
version =
    providers.gradleProperty("version").orNull ?: if (file(".git").exists()) {
        providers
            .exec {
                commandLine("git", "describe", "--tags", "--match", "v[0-9]*", "--always", "--dirty", "--abbrev=8")
                isIgnoreExitValue = true
            }.standardOutput.asText
            .get()
            .trim()
            .removePrefix("v")
            .ifEmpty { "0.0.0-SNAPSHOT" }
    } else {
        "0.0.0-SNAPSHOT"
    }
description = "Memocards application"

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
    mavenCentral()
}

val nodeRuntimeVersion = providers.fileContents(layout.projectDirectory.file(".nvmrc")).asText.map { it.trim() }

vaadin {
    frontendHotdeploy.set(true)
    nodeVersion.set(nodeRuntimeVersion.map { "v$it" })
}

node {
    version.set(nodeRuntimeVersion)
    download.set(true)
}

/*
 * Enhanced dependency management with BOMs and explicit versions
 */
apply(plugin = "io.spring.dependency-management")
extensions.configure<DependencyManagementExtension> {
    dependencies {
        dependency("com.google.errorprone:error_prone_annotations:${providers.gradleProperty("errorProneAnnotationsVersion").get()}")
        imports {
            mavenBom(BOM_COORDINATES)
            mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
        }
    }
}

dependencies {
    implementation("me.paulschwarz:spring-dotenv:$springDotenv")

    // Utilities
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("com.google.guava:guava:${providers.gradleProperty("guava").get()}")
    implementation("org.apache.commons:commons-lang3")

    // Vaadin
    implementation("com.vaadin:vaadin-core")
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.aspectj:aspectjweaver")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Dev tools
    developmentOnly("com.vaadin:vaadin-dev")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.assertj:assertj-core:$assertj")
    testImplementation("org.mockito:mockito-core:$mockito")

    // Test containers for integration tests
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion") // Using variable
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion") // Using variable
}

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
    }
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

        palantirJavaFormat(providers.gradleProperty("palantirJavaFormatVersion").get())
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
apply(plugin = "checkstyle")
extensions.configure<CheckstyleExtension> {
    toolVersion = providers.gradleProperty("checkstyleVersion").get()
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

/*
 * SpotBugs configuration - Strict mode
 */
apply(plugin = "com.github.spotbugs")
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    showProgress = true
    includeFilter = file("config/spotbugs/include.xml")
    excludeFilter = file("config/spotbugs/exclude.xml")
    effort = com.github.spotbugs.snom.Effort.MAX
    reports {
        create("xml") {
            required.set(true)
        }
        create("html") {
            required.set(true)
        }
    }
}

/*
 * PMD configuration
 */
apply(plugin = "pmd")
pmd {
    toolVersion = providers.gradleProperty("pmdVersion").get()
    ruleSetFiles = files("config/pmd/ruleset.xml")
    ruleSets = emptyList()
}

tasks.withType<Pmd>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Configure separate ruleset for test files
tasks.named<Pmd>("pmdTest") {
    ruleSetFiles = files("config/pmd/ruleset-test.xml")
}

/*
 * SonarLint configuration - Strict mode
 */
apply<name.remal.gradle_plugins.sonarlint.SonarLintPlugin>()
extensions.configure<SonarLintExtension> {
    languages { include("java") }
}

/*
 * OWASP Dependency Check configuration
 */
apply(plugin = "org.owasp.dependencycheck")

/*
 * Enhanced JaCoCo configuration with coverage verification
 */
apply(plugin = "jacoco")
jacoco {
    toolVersion = providers.gradleProperty("jacocoVersion").get()
}

tasks.withType<Test> {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Run tests"

    // Exclude only UI by default for all Test tasks
    useJUnitPlatform { }
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

// Integration test task for CI/CD
tasks.register<Test>("integrationTest") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Run integration tests with TestContainers"

    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    // Only run tests that extend BaseIntegrationTest
    include("**/*IntegrationTest.class")
    include("**/BaseIntegrationTest.class")

    // Enable TestContainers reuse for CI
    systemProperty("testcontainers.reuse.enable", "true")

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

// Configure npmInstall to run in the correct directory
tasks.named("npmInstall") {
    dependsOn("vaadinPrepareFrontend")

    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
}

// Both tasks use the lockfile when lint and production packaging run together.
tasks.named("vaadinBuildFrontend") { mustRunAfter("npmInstall") }

tasks.register("codeQuality") {
    description = "Runs core code quality checks (SonarLint, SpotBugs, Checkstyle, PMD)"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "sonarlintMain",
        "sonarlintTest",
        "spotbugsMain",
        "spotbugsTest",
        "checkstyleMain",
        "checkstyleTest",
        "pmdMain",
        "pmdTest",
    )
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("lintCss") {
    description = "Run stylelint for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    args.set(listOf("run", "lint:css"))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("lintCssFix") {
    description = "Run stylelint --fix for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    args.set(listOf("run", "lint:css:fix"))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("lintYaml") {
    description = "Run yamllint for Ansible YAML files"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    args.set(listOf("run", "lint:yaml"))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("lintYamlFix") {
    description = "Run prettier --write and yamllint for Ansible YAML files"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    args.set(listOf("run", "lint:yaml:fix"))
}

// Complete code quality check (everything)
tasks.register("codeQualityFull") {
    description = "Runs all code quality checks including CSS and YAML linting"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "codeQuality",
        "lintCss",
        "lintYaml",
    )
}

// Vaadin adds the frontend and its production token when it assembles the project JAR.
jib {
    containerizingMode = "packaged"
    configurationName.set("productionRuntimeClasspath")
    from {
        image = "eclipse-temurin:21-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/${project.findProperty("GITHUB_REPOSITORY") ?: "apolenkov/memocards"}"
        tags =
            setOf(
                "latest",
                project.version.toString(),
                System.getenv("GITHUB_SHA")?.take(7) ?: "latest",
            )
        auth {
            username = project.findProperty("GITHUB_ACTOR")?.toString() ?: "anonymous"
            password = project.findProperty("GITHUB_TOKEN")?.toString() ?: ""
        }
    }
    container {
        jvmFlags =
            listOf(
                "-XX:+UseContainerSupport",
                "-XX:MaxRAMPercentage=75.0",
                "-XX:+UseG1GC",
            )
        ports = listOf("8080")
        labels.set(
            mapOf(
                "org.opencontainers.image.source" to
                    "https://github.com/${project.findProperty("GITHUB_REPOSITORY") ?: "apolenkov/memocards"}",
                "org.opencontainers.image.description" to "Memocards Application - Flashcards Learning Platform",
                "org.opencontainers.image.licenses" to "MIT",
                "org.opencontainers.image.version" to project.version.toString(),
                "org.opencontainers.image.title" to "Memocards",
            ),
        )
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}

val verifyJibFrontend =
    tasks.register("verifyJibFrontend") {
        group = JavaBasePlugin.VERIFICATION_GROUP
        description = "Verify the Vaadin production frontend in the JAR that Jib puts on the image classpath"
        val archive = tasks.jar.flatMap { it.archiveFile }
        val mode = jib.containerizingMode
        inputs.file(archive)
        inputs.property("containerizingMode", mode)
        doLast {
            check(mode == "packaged") { "Jib must include the application JAR" }
            ZipFile(archive.get().asFile).use { jar ->
                val webapp = "META-INF/VAADIN/webapp"
                check(jar.getEntry("$webapp/index.html") != null) { "Missing Vaadin index.html" }
                check(jar.entries().asSequence().any { it.name.startsWith("$webapp/VAADIN/build/") && it.name.endsWith(".js") }) {
                    "Missing compiled Vaadin JavaScript"
                }
                val token = checkNotNull(jar.getEntry("META-INF/VAADIN/config/flow-build-info.json")) { "Missing Vaadin build token" }
                val info = jar.getInputStream(token).use { JsonSlurper().parse(it) as Map<*, *> }
                check(info["productionMode"] == true) { "Vaadin build token must enable production mode" }
            }
        }
    }

tasks.check { dependsOn(verifyJibFrontend) }

tasks.register("printJibMetadata") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Print the tags and version label configured for the container image"
    val tags = jib.to.tags
    val labels = jib.container.labels
    doLast {
        println("tags=" + tags.sorted().joinToString(","))
        println("org.opencontainers.image.version=" + labels.get().getValue("org.opencontainers.image.version"))
    }
}
