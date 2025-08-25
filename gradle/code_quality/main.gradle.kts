
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

// Create consolidated SonarLint report task
tasks.register("sonarlintConsolidatedReport") {
    description = "Generates consolidated SonarLint report from all sources"
    group = JavaBasePlugin.VERIFICATION_GROUP
    
    dependsOn("sonarlintMain", "sonarlintTest")
    
    doLast {
        val reportDir = file("${layout.buildDirectory.get()}/reports/sonarlint")
        reportDir.mkdirs()
        
        val consolidatedReport = file("${layout.buildDirectory.get()}/reports/sonarlint/consolidated-report.txt")
        
        // Analyze actual SonarLint results
        val mainReportDir = file("${layout.buildDirectory.get()}/reports/sonarlint/sonarlintMain")
        val testReportDir = file("${layout.buildDirectory.get()}/reports/sonarlint/sonarlintTest")
        
        val mainIssues = analyzeSonarLintReport(mainReportDir)
        val testIssues = analyzeSonarLintReport(testReportDir)
        
        val totalIssues = mainIssues + testIssues
        
        // Create simple text report
        consolidatedReport.writeText("""
            SonarLint Consolidated Report
            =============================
            
            Project: ${project.name}
            Version: ${project.version}
            Generated: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
            
            Summary:
            - Total Issues: $totalIssues
            - Main Source Issues: $mainIssues
            - Test Source Issues: $testIssues
            
            Sources:
            - Main: ${if (mainIssues > 0) "$mainIssues issues" else "No issues"}
            - Test: ${if (testIssues > 0) "$testIssues issues" else "No issues"}
            
            Command: ./gradlew sonarlintConsolidatedReport
        """.trimIndent())
        
        logger.lifecycle("✅ SonarLint consolidated report generated!")
        logger.lifecycle("📊 Total issues: $totalIssues")
        logger.lifecycle("📁 Report location: ${consolidatedReport}")
    }
}

// Helper function to analyze SonarLint report directory
fun analyzeSonarLintReport(reportDir: File): Int {
    return if (reportDir.exists() && reportDir.isDirectory) {
        val htmlFile = File(reportDir, "sonarlint.html")
        if (htmlFile.exists()) {
            // Simple analysis - count issues by looking for patterns in HTML
            val content = htmlFile.readText()
            // Count issue patterns (this is a simplified approach)
            content.split("issue").size - 1
        } else {
            0
        }
    } else {
        0
    }
}

// Create unified code quality task
tasks.register("codeQuality") {
    description = "Runs all code quality checks (SonarLint, SpotBugs, Checkstyle)"
    group = JavaBasePlugin.VERIFICATION_GROUP

    dependsOn(
        "sonarlintConsolidatedReport",
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