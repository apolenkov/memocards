import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import name.remal.gradle_plugins.sonarlint.SonarLintExtension
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

val javaTargetVersion = JavaVersion.VERSION_21
val vaadinVersion: String by project
val lineAwesome: String by project
val assertj: String by project
val mockito: String by project
val webdrivermanager: String by project
val springDotenv: String by project
val testcontainersVersion: String by project

plugins {
    idea
    id("java")

    id("fr.brouillard.oss.gradle.jgitver")
    id("org.springframework.boot")
    id("com.github.node-gradle.node")
    id("com.vaadin")

    id("io.spring.dependency-management")
    id("org.owasp.dependencycheck")

    id("com.diffplug.spotless")
    id("name.remal.sonarlint")
    id("com.github.spotbugs")
    id("checkstyle")

    id("jacoco")

    id("com.google.cloud.tools.jib")
}

group = "org.apolenkov.application"
version = "0.1"
description = "Memocards application" // Rename to memocards

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
    mavenCentral()
    maven(url = "https://maven.vaadin.com/vaadin-addons")
}

node {
    version.set("20.18.0")
    download.set(true)
}

/*
 * Enhanced dependency management with BOMs and explicit versions
 */
apply(plugin = "io.spring.dependency-management")
extensions.configure<DependencyManagementExtension> {
    dependencies {
        imports {
            mavenBom(BOM_COORDINATES)
            mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
        }
    }
}

dependencies {
    implementation("me.paulschwarz:spring-dotenv:$springDotenv")

    // Utilities
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")

    // Vaadin
    implementation("com.vaadin:vaadin")
    implementation("com.vaadin:vaadin-spring-boot-starter")

    implementation("org.parttio:line-awesome:$lineAwesome")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.aspectj:aspectjweaver")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.vaadin:vaadin-testbench-junit5")

    testImplementation("org.assertj:assertj-core:$assertj")
    testImplementation("org.mockito:mockito-core:$mockito")
    testImplementation("io.github.bonigarcia:webdrivermanager:$webdrivermanager")

    // Test containers for integration tests
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion") // Using variable
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion") // Using variable
}

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

        // Existing force declarations
        force("commons-io:commons-io:2.18.0")
        force("org.checkerframework:checker-qual:3.48.3")

        // Resolve reported conflicts with latest versions
        force("org.apache.commons:commons-compress:1.27.1")
        force("com.google.guava:guava:33.4.5-jre")
        force("com.google.errorprone:error_prone_annotations:2.36.0")
        force("io.github.classgraph:classgraph:4.8.179")

        // Added to resolve the reported conflicts
        force("com.github.docker-java:docker-java-api:3.6.0")
        force("com.github.docker-java:docker-java-transport:3.6.0")
        force("org.jetbrains:annotations:17.0.0")
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
apply(plugin = "checkstyle")
extensions.configure<CheckstyleExtension> {
    toolVersion = "10.12.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

/*
 * SpotBugs configuration
 */
apply(plugin = "com.github.spotbugs")
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    showProgress = true
    includeFilter = file("config/spotbugs/include.xml")
    reports {
        create("xml") {
            required.set(true)
        }
    }
}

/*
 * SonarLint configuration
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

tasks.register("codeQuality") {
    description = "Runs core code quality checks (SonarLint, SpotBugs, Checkstyle)"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "sonarlintMain",
        "sonarlintTest",
        "spotbugsMain",
        "spotbugsTest",
        "checkstyleMain",
        "checkstyleTest",
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

// Complete code quality check (everything)
tasks.register("codeQualityFull") {
    description = "Runs all code quality checks including CSS linting and i18n"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "codeQuality",
        "lintCss",
    )
}

// Ensure frontend is built for production mode before Jib
tasks.named("jibDockerBuild") {
    dependsOn("vaadinBuildFrontend")
}

tasks.named("jib") {
    dependsOn("vaadinBuildFrontend")
}

// Jib configuration for different profiles
jib {
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
        tags = setOf("latest", System.getenv("GITHUB_SHA")?.take(7) ?: "latest")
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
