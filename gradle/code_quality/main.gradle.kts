
/*
 * Code Quality Tasks Configuration
 */

// Configure conditional execution for Checkstyle
afterEvaluate {
    tasks.named("checkstyleMain") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
    tasks.named("checkstyleTest") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
}

// Configure conditional execution for SpotBugs
afterEvaluate {
    tasks.named("spotbugsMain") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
    tasks.named("spotbugsTest") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
}

// Configure conditional execution for SonarLint
afterEvaluate {
    tasks.named("sonarlintMain") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
    tasks.named("sonarlintTest") {
        onlyIf { gradle.taskGraph.hasTask(":codeQuality") }
    }
}

// Create unified code quality task
tasks.register("codeQuality") {
    description = "Runs all code quality checks (SonarLint, SpotBugs, Checkstyle)"
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

apply(from = "gradle/code_quality/i18n-checks.gradle.kts")
apply(from = "gradle/code_quality/translations-missing.gradle.kts")
apply(from = "gradle/code_quality/non-english.gradle.kts")

tasks.register("codeQualityChars") {
    description = "Runs all code quality checks (SonarLint, SpotBugs, Checkstyle)"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "checkNonEnglishCharacters",
        "checkI18nKeys",
        "checkMissingTranslations",
    )
}

// CSS linting tasks
tasks.register("lintCss") {
    description = "Run stylelint for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    
    doLast {
        exec {
            workingDir = project.projectDir
            commandLine("npm", "run", "lint:css")
        }
    }
}

tasks.register("lintCssFix") {
    description = "Run stylelint --fix for CSS in themes"
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("npmInstall")
    mustRunAfter("vaadinPrepareFrontend")
    
    doLast {
        exec {
            workingDir = project.projectDir
            commandLine("npm", "run", "lint:css:fix")
        }
    }
}

// Enhanced code quality task with CSS linting
tasks.register("codeQualityFull") {
    description = "Runs all code quality checks including CSS linting"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "codeQuality",
        "codeQualityChars",
        "lintCss"
    )
}