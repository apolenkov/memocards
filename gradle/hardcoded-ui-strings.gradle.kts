import java.lang.StringBuilder
/**
 * Task to check for hardcoded UI strings that should use translations
 * Finds hardcoded text in UI components that should use getTranslation
 */
tasks.register("checkHardcodedUIStrings") {
    group = "verification"
    description = "Check for hardcoded UI strings that should use translations"

    doLast {
        val report = StringBuilder()
        report.appendLine("# Hardcoded UI Strings Report")
        report.appendLine()

        val sourceFiles =
            fileTree("src/main/java")
                .matching {
                    include("**/*.java")
                }.files

        val violations = mutableListOf<Violation>()

        // Patterns for hardcoded UI strings
        val patterns =
            listOf(
                // TextField, TextArea, Button constructors with hardcoded strings
                Regex("new\\s+(TextField|TextArea|Button|H1|H2|H3|H4|H5|H6|Paragraph|Span|Div|Label)\\s*\\(\\s*\"([^\"]{3,})\""),
                // setText with hardcoded strings
                Regex("\\.setText\\s*\\(\\s*\"([^\"]{3,})\""),
                // setPlaceholder with hardcoded strings
                Regex("\\.setPlaceholder\\s*\\(\\s*\"([^\"]{3,})\""),
                // setHelperText with hardcoded strings
                Regex("\\.setHelperText\\s*\\(\\s*\"([^\"]{3,})\""),
                // setErrorMessage with hardcoded strings
                Regex("\\.setErrorMessage\\s*\\(\\s*\"([^\"]{3,})\""),
                // setHeader with hardcoded strings
                Regex("\\.setHeader\\s*\\(\\s*\"([^\"]{3,})\""),
                // setTitle with hardcoded strings
                Regex("\\.setTitle\\s*\\(\\s*\"([^\"]{3,})\""),
                // setAttribute title with hardcoded strings
                Regex("\\.setAttribute\\s*\\(\\s*\"title\"\\s*,\\s*\"([^\"]{3,})\""),
                // setProperty title with hardcoded strings
                Regex("\\.setProperty\\s*\\(\\s*\"title\"\\s*,\\s*\"([^\"]{3,})\""),
            )

        // Common words that are likely not hardcoded strings
        val commonWords =
            setOf(
                "id",
                "class",
                "style",
                "width",
                "height",
                "margin",
                "padding",
                "border",
                "color",
                "background",
                "font",
                "size",
                "weight",
                "align",
                "center",
                "left",
                "right",
                "top",
                "bottom",
                "position",
                "display",
                "flex",
                "grid",
                "block",
                "inline",
                "none",
                "hidden",
                "visible",
                "opacity",
                "z-index",
                "cursor",
                "pointer",
                "default",
                "auto",
                "inherit",
                "initial",
                "unset",
                "revert",
                "revert-layer",
            )

        sourceFiles.forEach { file ->
            val content = file.readLines()
            val relativePath = file.relativeTo(project.projectDir)

            content.forEachIndexed { lineIndex, line ->
                patterns.forEach { pattern ->
                    val matches = pattern.findAll(line)
                    matches.forEach { match ->
                        val text = match.groupValues.lastOrNull()
                        if (text != null && text.length > 3 &&
                            !text.matches(
                                Regex("^[\\d\\s\\-\\_\\.\\,\\(\\)\\[\\]\\{\\}\\+\\*\\/\\\\\\|\\&\\^\\%\\$\\#\\@\\!\\?\\<\\>\\=\\~\\`]+$"),
                            )
                        ) {
                            // Check if it's not a common CSS property or technical term
                            val words = text.split("\\s+").filter { it.length > 2 }
                            val hasHumanReadableText =
                                words.any { word ->
                                    word.length > 2 && !commonWords.contains(word.lowercase()) &&
                                        word.matches(Regex(".*[а-яёa-zA-Z].*"))
                                }

                            if (hasHumanReadableText) {
                                violations.add(Violation(relativePath.toString(), lineIndex + 1, text, line.trim()))
                            }
                        }
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            report.appendLine("## Hardcoded UI Strings Found")
            report.appendLine()
            report.appendLine("The following hardcoded strings should be replaced with translation keys:")
            report.appendLine()

            violations.groupBy { it.file }.forEach { (file, fileViolations) ->
                report.appendLine("### $file")
                report.appendLine()
                fileViolations.forEach { violation ->
                    report.appendLine("- **Line ${violation.lineNumber}**: `${violation.text}`")
                    report.appendLine("  ```java")
                    report.appendLine("  ${violation.context}")
                    report.appendLine("  ```")
                    report.appendLine()
                }
            }

            report.appendLine("## Summary")
            report.appendLine()
            report.appendLine("- **Files with violations**: ${violations.groupBy { it.file }.size}")
            report.appendLine("- **Total violations**: ${violations.size}")
            report.appendLine()
            report.appendLine("Please replace hardcoded strings with appropriate translation keys using `getTranslation()`." )

            val outFile =
                layout.buildDirectory
                    .file("reports/translations/hardcoded-ui-strings.md")
                    .get()
                    .asFile
            outFile.parentFile.mkdirs()
            outFile.writeText(report.toString())

            println("❌ Found ${violations.size} hardcoded UI strings that should use translations")
            println("[checkHardcodedUIStrings] Report generated at ${outFile.relativeTo(project.projectDir)}")

            // Don't fail the build for this check, just warn
            println("⚠️  This is a warning - please review and fix hardcoded UI strings")
        } else {
            report.appendLine("✅ No hardcoded UI strings found.")
            report.appendLine()
            report.appendLine("All UI text appears to use proper translation keys.")

            val outFile =
                layout.buildDirectory
                    .file("reports/translations/hardcoded-ui-strings.md")
                    .get()
                    .asFile
            outFile.parentFile.mkdirs()
            outFile.writeText(report.toString())

            println("✅ No hardcoded UI strings found")
            println("[checkHardcodedUIStrings] Report generated at ${outFile.relativeTo(project.projectDir)}")
        }
    }
}

// Data class for violations
data class Violation(
    val file: String,
    val lineNumber: Int,
    val text: String,
    val context: String,
)

// Keep as a soft-check: do not fail the build, only warn, so no dependsOn added to build here


