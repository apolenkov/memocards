import com.github.gradle.node.npm.task.NpmTask
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
    id("com.github.node-gradle.node") version "7.0.2"
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
    mavenCentral()
    maven(url = "https://maven.vaadin.com/vaadin-addons")
}

// Node/Stylelint integration
node {
    version.set("20.18.0")
    download.set(true)
}

// Configure npmInstall to run in the correct directory
tasks.named("npmInstall") {
    // Ensure Vaadin prepares/updates package.json before npm install
    dependsOn("vaadinPrepareFrontend")

    // Correctly declare inputs as files (lock file is optional for our use)
    inputs.file("package.json")
    outputs.dir("node_modules")
}

tasks.register<NpmTask>("lintCss") {
    description = "Run stylelint for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    args.set(listOf("run", "lint:css"))

    // Ensure the task runs in the correct directory
    workingDir.set(project.projectDir)
}

tasks.register<NpmTask>("lintCssFix") {
    description = "Run stylelint --fix for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    args.set(listOf("run", "lint:css:fix"))

    // Ensure the task runs in the correct directory
    workingDir.set(project.projectDir)
}

// Add CSS linting to check task
tasks.named("check") {
    dependsOn("lintCss")
    mustRunAfter("vaadinPrepareFrontend")
}

// Apply JavaDoc quality report generation
apply(from = "gradle/javadoc-quality-report.gradle.kts")

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
        }

        // Explicit dependency versions for better control
        val asm: String by project
        val guava: String by project
        val commonsLang3: String by project
        dependency("org.ow2.asm:asm-commons:$asm")
        dependency("com.google.guava:guava:$guava")
        dependency("org.apache.commons:commons-lang3:$commonsLang3")
    }
}

dependencies {
    // Utilities
    implementation("com.google.guava:guava")
    implementation("org.apache.commons:commons-lang3")

    // Vaadin
    implementation("com.vaadin:vaadin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    val lineAwesome: String by project
    implementation("org.parttio:line-awesome:$lineAwesome")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Persistence
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.vaadin:vaadin-testbench-junit5")
    val assertj: String by project
    val mockito: String by project
    val webdrivermanager: String by project
    testImplementation("org.assertj:assertj-core:$assertj")
    testImplementation("org.mockito:mockito-core:$mockito")
    testImplementation("io.github.bonigarcia:webdrivermanager:$webdrivermanager")

    // Centralized constraints instead of resolutionStrategy.force
    constraints {
        val asm: String by project
        implementation("commons-io:commons-io") { version { strictly("2.19.0") } }
        implementation("org.checkerframework:checker-qual") { version { strictly("3.45.0") } }
        implementation("org.apache.commons:commons-compress") { version { strictly("1.27.1") } }
        implementation("org.jetbrains:annotations") { version { strictly("17.0.0") } }
        implementation("io.github.classgraph:classgraph") { version { strictly("4.8.179") } }
        implementation("com.google.errorprone:error_prone_annotations") { version { strictly("2.38.0") } }
        implementation("org.ow2.asm:asm") { version { strictly(asm) } }
        implementation("org.ow2.asm:asm-tree") { version { strictly(asm) } }
        implementation("org.ow2.asm:asm-analysis") { version { strictly(asm) } }
        implementation("org.ow2.asm:asm-util") { version { strictly(asm) } }
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

// Checkstyle temporarily disabled due to configuration issues
// apply(plugin = "checkstyle")
// extensions.configure<org.gradle.api.plugins.quality.CheckstyleExtension> {
//     toolVersion = "10.12.1"
//     configFile = file("config/checkstyle/checkstyle.xml")
//     isShowViolations = true
//     maxWarnings = 0
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
 * Force specific versions for SonarLint dependencies to resolve conflicts
 */

//    configurations.matching { it.name.startsWith("sonarlint") }.configureEach {
//        resolutionStrategy {
//            force("commons-io:commons-io:2.19.0")
//            force("org.checkerframework:checker-qual:3.45.0")
//            force("com.google.errorprone:error_prone_annotations:2.38.0")
//            force("org.jetbrains:annotations:17.0.0")
//            force("org.apache.commons:commons-compress:1.27.1")
//            force("io.github.classgraph:classgraph:4.8.179")
//        }
//    }

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
    useJUnitPlatform { excludeTags("ui") }
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
    // Avoid formatting during compilation in CI; run explicitly in lint stage
}

/*
 * Enhanced check task with all quality gates
 */
tasks.named("check") {
    description = "Run all verification tasks including formatting, static analysis, and tests"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "spotlessCheck",
        "lintCss",
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

// Basic UI TestBench config hint: runs as regular junit5 tests
configurations.testImplementation {
    val seleniumJava: String by project
    resolutionStrategy { force("org.seleniumhq.selenium:selenium-java:$seleniumJava") }
}

// Separate UI test task that only runs @Tag("ui") tests
tasks.register<Test>("uiTest") {
    description = "Runs UI (TestBench) tests"
    group = JavaBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("ui")
    }
    shouldRunAfter(tasks.test)
}

// Separate Integration test task that only runs @Tag("integration") tests
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = JavaBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform { includeTags("integration") }
    shouldRunAfter(tasks.test)
}

// Ensure unit test task excludes integration explicitly
tasks.named<Test>("test").configure {
    useJUnitPlatform { excludeTags("integration", "ui") }
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
    }
}

// Gradle dependency locking
dependencyLocking {
    lockAllConfigurations()
}

configurations.named("jacocoAnt").configure {
    resolutionStrategy {
        val asm: String by project
        force("org.ow2.asm:asm:$asm")
        force("org.ow2.asm:asm-tree:$asm")
        force("org.ow2.asm:asm-analysis:$asm")
        force("org.ow2.asm:asm-util:$asm")
    }
}

// Spotless creates dynamic configurations (e.g. :spotless12345). Align their dependency versions.
configurations.configureEach {
    if (name.startsWith("spotless")) {
        resolutionStrategy {
            force("org.checkerframework:checker-qual:3.45.0")
            force("com.google.errorprone:error_prone_annotations:2.38.0")
            force("org.jetbrains:annotations:17.0.0")
            force("commons-io:commons-io:2.19.0")
            force("org.apache.commons:commons-compress:1.27.1")
            force("io.github.classgraph:classgraph:4.8.179")
        }
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

// Apply split custom tasks
apply(from = "gradle/i18n-checks.gradle.kts")
apply(from = "gradle/code-quality-non-english.gradle.kts")
apply(from = "gradle/translations-missing.gradle.kts")
apply(from = "gradle/hardcoded-ui-strings.gradle.kts")
apply(from = "gradle/vaadin-clean.gradle.kts")
