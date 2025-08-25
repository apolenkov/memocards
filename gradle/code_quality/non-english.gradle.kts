import java.lang.StringBuilder
/*
 * Custom task for checking non-English characters in code
 * Writes a report to build/reports/non-english-chars/non-english-chars.md and fails on violations.
 */
tasks.register("checkNonEnglishCharacters") {
    group = "verification"
    description = "Check for non-English characters in source code files according to project rules"

    doLast {
        val sourceFiles =
            fileTree("src") {
                include("**/*.java", "**/*.xml", "**/*.yml", "**/*.properties")
                exclude("**/i18n/**", "**/build/**", "**/node_modules/**", "**/*Test.java", "**/test/**")
            }

        // Pattern for specific non-English alphabets we want to catch
        val specificNonEnglishPattern =
            Regex("""[а-яёА-ЯЁ\u4e00-\u9fff\u3040-\u309f\u30a0-\u30ff\u0590-\u05ff\u0600-\u06ff\u0750-\u077f]""")

        // Pattern for problematic punctuation that should be replaced with ASCII equivalents
        val problematicPunctuationPattern =
            Regex(
                """[\u2013\u2014\u2015\u2018\u2019\u201C\u201D\u2026]""",
            ) // en dash, em dash, fancy quotes, ellipsis

        val report = StringBuilder()
        report.appendLine("# Non-English Characters Report")
        report.appendLine()
        report.appendLine("This report shows non-English characters found in source code files.")
        report.appendLine(
            "According to project rule 'my-rule: " +
                "All code in project use english language', " +
                "only English characters are allowed.",
        )
        report.appendLine()

        var totalViolations = 0
        var filesWithViolations = 0

        sourceFiles.forEach { file ->
            val content = file.readText()
            val problematicPunctuationMatches = problematicPunctuationPattern.findAll(content)
            val specificMatches = specificNonEnglishPattern.findAll(content)
            val allMatches = (problematicPunctuationMatches + specificMatches).distinctBy { it.range }

            if (allMatches.any()) {
                filesWithViolations++
                val relativePath = file.relativeTo(project.projectDir)
                report.appendLine("## $relativePath")
                report.appendLine()

                val matchesList = allMatches.toList()
                totalViolations += matchesList.size

                // Show console output as before
                println("❌ ${file.path} contains non-English characters:")

                matchesList.take(10).forEach { match ->
                    val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
                    val char = match.value
                    val unicodePoint = char.codePointAt(0)
                    val (charDescription, suggestion) =
                        when {
                            char.matches(Regex("[а-яёА-ЯЁ]")) -> "Cyrillic" to "Use English characters"
                            char.matches(Regex("[\u4e00-\u9fff]")) -> "Chinese" to "Use English characters"
                            char.matches(Regex("[\u3040-\u309f]")) -> "Hiragana" to "Use English characters"
                            char.matches(Regex("[\u30a0-\u30ff]")) -> "Katakana" to "Use English characters"
                            char.matches(Regex("[\u0590-\u05ff]")) -> "Hebrew" to "Use English characters"
                            char.matches(Regex("[\u0600-\u06ff]")) -> "Arabic" to "Use English characters"
                            char == "\u2013" -> "En dash" to "Replace with hyphen (-)"
                            char == "\u2014" -> "Em dash" to "Replace with hyphen (-) or double hyphen (--)"
                            char == "\u2015" -> "Horizontal bar" to "Replace with hyphen (-)"
                            char == "\u2018" || char == "\u2019" -> "Curly single quote" to "Replace with straight quote (')"
                            char == "\u201C" || char == "\u201D" -> "Curly double quote" to "Replace with straight quote (\")"
                            char == "\u2026" -> "Ellipsis" to "Replace with three dots (...)"
                            else -> "Non-English" to "Use English characters only"
                        }

                    // Console output
                    println("   Line $lineNumber: '$char' (U+${unicodePoint.toString(16).uppercase()}, $charDescription) → $suggestion")

                    // Report output
                    report.appendLine("- **Line $lineNumber**: `$char` (U+${unicodePoint.toString(16).uppercase()}) - $charDescription")
                    report.appendLine("  - **Suggestion**: $suggestion")
                }

                if (matchesList.size > 10) {
                    val remaining = matchesList.size - 10
                    println("   ... and $remaining more non-English characters")
                    report.appendLine("- ... and $remaining more non-English characters")
                }
                println()
                report.appendLine()
            }
        }

        // Generate summary
        if (totalViolations > 0) {
            report.appendLine("## Summary")
            report.appendLine()
            report.appendLine("- **Files with violations**: $filesWithViolations")
            report.appendLine("- **Total violations**: $totalViolations")
            report.appendLine()
            report.appendLine("Please fix these violations to ensure code consistency and maintainability.")
        } else {
            report.clear()
            report.appendLine("# Non-English Characters Report")
            report.appendLine()
            report.appendLine("✅ No non-English characters found in source code.")
            report.appendLine()
            report.appendLine("All source files comply with the project rule requiring English characters only.")
        }

        val outFile =
            layout.buildDirectory
                .file("reports/non-english-chars/non-english-chars.md")
                .get()
                .asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(report.toString())

        if (totalViolations > 0) {
            throw GradleException(
                "[non-english-chars] Found $totalViolations non-English characters in $filesWithViolations files. " +
                    "See ${outFile.relativeTo(project.projectDir)} for detailed report.",
            )
        } else {
            println("✅ No non-English characters found in source code")
            println("[non-english-chars] Report generated at ${outFile.relativeTo(project.projectDir)}")
        }
    }
}
