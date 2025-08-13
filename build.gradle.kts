plugins {
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    id("com.vaadin")
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    id("jacoco")
}

group = "org.apolenkov.application"
version = "1.0-SNAPSHOT"
description = "flashcards"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven(url = "https://maven.vaadin.com/vaadin-addons")
}

// Use Spring Boot's predefined 'developmentOnly' configuration; no manual creation

dependencies {
    // Vaadin
    implementation("com.vaadin:vaadin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.parttio:line-awesome:2.1.0")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Persistence (enable via profiles memory/jpa)
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
    // Testcontainers for integration tests
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
}

dependencyManagement {
    imports {
        val vaadinVersion: String by project
        mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
    }
}

// Default task
defaultTasks("bootRun")

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.17.0")
        importOrder("java", "javax", "jakarta", "org", "com", "")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "10.12.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    isShowViolations = true
}

tasks.named("spotlessJava") {
    dependsOn("vaadinPrepareFrontend")
}

tasks.check {
    description =
        "Check formatting and SonarLint for this project and all subprojects."
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn("spotlessCheck")
//    dependsOn("sonarlintMain")
//    dependsOn("sonarlintTest")
    dependsOn("checkstyleMain")
    dependsOn("checkstyleTest")
//    dependsOn("spotbugsMain")
//    dependsOn("spotbugsTest")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.withType<JacocoCoverageVerification> {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named("spotlessCheck"))
}
