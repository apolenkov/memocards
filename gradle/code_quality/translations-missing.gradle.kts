import java.lang.StringBuilder

/**
 * Task to check for missing translation keys
 * Finds translation keys used in code but missing from properties files
 */
tasks.register("checkMissingTranslations") {
    group = "verification"
    description = "Check for missing translation keys in code"

    doLast {
        val report = StringBuilder()
        report.appendLine("# Missing Translation Keys Report")
        report.appendLine()

        val sourceFiles =
            fileTree("src/main/java")
                .matching {
                    include("**/*.java")
                }.files

        val propertiesFiles =
            fileTree("src/main/resources/i18n")
                .matching {
                    include("**/*.properties")
                }.files

        // Extract all translation keys from properties files
        val availableKeys = mutableSetOf<String>()
        propertiesFiles.forEach { file ->
            file.readLines().forEach { line ->
                if (line.contains("=") && !line.startsWith("#")) {
                    val key = line.substringBefore("=").trim()
                    availableKeys.add(key)
                }
            }
        }

        // Find all translation usage patterns in source code
        val usedKeys = mutableSetOf<String>()
        val missingKeys = mutableSetOf<String>()

        // Multiple patterns to catch different translation usage
        val translationPatterns =
            listOf(
                Regex("getTranslation\\(\"([^\"]+)\""), // getTranslation("key")
                Regex("getTranslation\\(\\s*\"([^\"]+)\""), // getTranslation( "key")
                Regex("I18nHelper\\.tr\\(\"([^\"]+)\""), // I18nHelper.tr("key")
                Regex("I18nHelper\\.tr\\(\\s*\"([^\"]+)\""), // I18nHelper.tr( "key")
                Regex("\\.tr\\(\"([^\"]+)\""), // .tr("key")
                Regex("\\.tr\\(\\s*\"([^\"]+)\""), // .tr( "key")
                Regex("tr\\(\"([^\"]+)\""), // tr("key")
                Regex("tr\\(\\s*\"([^\"]+)\""), // tr( "key")
                Regex("getTranslation\\(\"([^\"]+)\"\\s*,\\s*[^)]+\\)"), // getTranslation("key", params)
                Regex("I18nHelper\\.tr\\(\"([^\"]+)\"\\s*,\\s*[^)]+\\)"), // I18nHelper.tr("key", params)
                Regex("\\.tr\\(\"([^\"]+)\"\\s*,\\s*[^)]+\\)"), // .tr("key", params)
                Regex("tr\\(\"([^\"]+)\"\\s*,\\s*[^)]+\\)"), // tr("key", params)
            )

        // Also look for constants that contain translation keys
        val constantPatterns =
            listOf(
                Regex("private static final String [A-Z_]+ = \"([^\"]+)\""), // private static final String KEY = "key"
                Regex("public static final String [A-Z_]+ = \"([^\"]+)\""), // public static final String KEY = "key"
                Regex("final String [A-Z_]+ = \"([^\"]+)\""), // final String KEY = "key"
                Regex("String [A-Z_]+ = \"([^\"]+)\""), // String KEY = "key"
            )

        sourceFiles.forEach { file ->
            val content = file.readText()

            // Check direct translation calls
            translationPatterns.forEach { pattern ->
                val matches = pattern.findAll(content)
                matches.forEach { match ->
                    val key = match.groupValues[1]
                    usedKeys.add(key)
                    if (!availableKeys.contains(key)) {
                        missingKeys.add(key)
                    }
                }
            }

            // Check constants that might contain translation keys
            constantPatterns.forEach { pattern ->
                val matches = pattern.findAll(content)
                matches.forEach { match ->
                    val key = match.groupValues[1]
                    // Only consider it if it looks like a translation key (contains dots)
                    // Exclude technical keys that are not actual translation keys
                    if (key.contains(".") && !key.startsWith("http") && !key.startsWith("/") &&
                        !key.equals("i18n.messages")
                    ) {
                        usedKeys.add(key)
                        if (!availableKeys.contains(key)) {
                            missingKeys.add(key)
                        }
                    }
                }
            }
        }

        // Find unused keys
        val unusedKeys = availableKeys - usedKeys

        if (missingKeys.isNotEmpty() || unusedKeys.isNotEmpty()) {
            report.appendLine("## Missing Translation Keys")
            report.appendLine()
            missingKeys.sorted().forEach { key ->
                report.appendLine("- `$key`")
            }

            if (unusedKeys.isNotEmpty()) {
                report.appendLine()
                report.appendLine("## Unused Translation Keys")
                report.appendLine()
                unusedKeys.sorted().forEach { key ->
                    report.appendLine("- `$key`")
                }
            }

            report.appendLine()
            report.appendLine("## Summary")
            report.appendLine()
            report.appendLine("- **Missing keys**: ${missingKeys.size}")
            report.appendLine("- **Unused keys**: ${unusedKeys.size}")
            report.appendLine("- **Total available keys**: ${availableKeys.size}")
            report.appendLine("- **Total used keys**: ${usedKeys.size}")
            report.appendLine()
            report.appendLine("Please add missing translation keys to all language files.")

            val outFile =
                layout.buildDirectory
                    .file("reports/translations/missing-translations.md")
                    .get()
                    .asFile
            outFile.parentFile.mkdirs()
            outFile.writeText(report.toString())

            if (missingKeys.isNotEmpty()) {
                throw GradleException(
                    "[checkMissingTranslations] Found ${missingKeys.size} missing translation keys. " +
                        "See ${outFile.relativeTo(project.projectDir)} for detailed report.",
                )
            }
        } else {
            report.appendLine("✅ All translation keys are properly defined and used.")
            report.appendLine()
            report.appendLine("- **Total available keys**: ${availableKeys.size}")
            report.appendLine("- **Total used keys**: ${usedKeys.size}")

            val outFile =
                layout.buildDirectory
                    .file("reports/translations/missing-translations.md")
                    .get()
                    .asFile
            outFile.parentFile.mkdirs()
            outFile.writeText(report.toString())

            println("✅ All translation keys are properly defined and used")
            println("[checkMissingTranslations] Report generated at ${outFile.relativeTo(project.projectDir)}")
        }
    }
}
