import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * Task to generate beautiful JavaDoc quality report
 * Analyzes JavaDoc comments and generates formatted report
 */
tasks.register("generateJavadocReport") {
    group = "verification"
    description = "Generate beautiful JavaDoc quality report"

    doLast {
        val report = StringBuilder()
        report.appendLine("# JavaDoc Quality Report")
        report.appendLine()
        report.appendLine("Generated: ${java.time.LocalDateTime.now()}")
        report.appendLine()
        report.appendLine("## 📊 Summary")
        report.appendLine()

        val sourceDir = project.file("src/main/java")
        val issues = mutableListOf<String>()
        val totalComments = mutableListOf<CommentInfo>()
        
        if (sourceDir.exists()) {
            scanJavaFiles(sourceDir, issues, totalComments)
        }

        // Generate summary
        val criticalCount = issues.count { it.startsWith("CRITICAL") }
        val warningCount = issues.count { it.startsWith("WARNING") }
        val infoCount = issues.count { it.startsWith("INFO") }

        report.appendLine("- **Total comments analyzed**: ${totalComments.size}")
        report.appendLine("- **Critical issues**: ${criticalCount}")
        report.appendLine("- **Warnings**: ${warningCount}")
        report.appendLine("- **Info**: ${infoCount}")
        report.appendLine()

        // Comment length distribution (effective length excluding @tags)
        val lengthDistribution = totalComments.groupBy { it.length }.toSortedMap()
        report.appendLine("## 📈 Comment Length Distribution (Effective)")
        report.appendLine()
        report.appendLine("*Effective length excludes @param, @return, @throws, @see, @deprecated, @since, @author, @version tags*")
        report.appendLine()
        lengthDistribution.forEach { (length, comments) ->
            val emoji = when {
                length <= 5 -> "✅"
                length <= 10 -> "⚠️"
                length <= 15 -> "🔴"
                else -> "💀"
            }
            report.appendLine("$emoji **$length lines**: ${comments.size} comments")
        }
        report.appendLine()

        // Issues by severity
        if (issues.isNotEmpty()) {
            report.appendLine("## 🚨 Issues by Severity")
            report.appendLine()
            
            issues.groupBy { it.split(": ")[0] }.forEach { (severity, severityIssues) ->
                val emoji = when (severity) {
                    "CRITICAL" -> "💀"
                    "WARNING" -> "⚠️"
                    "INFO" -> "ℹ️"
                    else -> "❓"
                }
                report.appendLine("$emoji **$severity** (${severityIssues.size}):")
                severityIssues.forEach { issue ->
                    val details = issue.split(": ")[1]
                    report.appendLine("  - `$details`")
                }
                report.appendLine()
            }
        }

        // Top problematic files
        val fileIssues = issues.groupBy { it.split(":")[0] }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        if (fileIssues.isNotEmpty()) {
            report.appendLine("## 🔥 Top Problematic Files")
            report.appendLine()
            fileIssues.forEach { (file, count) ->
                val fileName = file.substringAfterLast("/")
                report.appendLine("- **$fileName**: $count issues")
            }
            report.appendLine()
        }

        // Recommendations
        report.appendLine("## 💡 Recommendations")
        report.appendLine()
        report.appendLine("### Comments by Length (Effective):")
        report.appendLine("- **≤ 5 lines**: ✅ Usually good, no action needed")
        report.appendLine("- **6-10 lines**: ⚠️ Check for excessive detail")
        report.appendLine("- **11-15 lines**: 🔴 Likely too verbose, consider refactoring")
        report.appendLine("- **> 15 lines**: 💀 Critical - requires immediate refactoring")
        report.appendLine()
        report.appendLine("*Note: Effective length excludes @param, @return, @throws, @see, @deprecated, @since, @author, @version tags*")
        report.appendLine()
        report.appendLine("### Content Guidelines:")
        report.appendLine("- Focus on **business purpose**, not technical implementation")
        report.appendLine("- Use active voice: \"Returns\", \"Sets\", \"Creates\"")
        report.appendLine("- Avoid HTML tags and excessive lists")
        report.appendLine("- Keep it concise: \"capturing the essence without excessive detail\"")
        report.appendLine()

        // Quality score
        val qualityScore = calculateQualityScore(totalComments, issues)
        report.appendLine("## 🎯 Quality Score")
        report.appendLine()
        report.appendLine("**Overall Quality**: $qualityScore%")
        report.appendLine()
        
        val grade = when {
            qualityScore >= 90 -> "🟢 A+ (Excellent)"
            qualityScore >= 80 -> "🟢 A (Very Good)"
            qualityScore >= 70 -> "🟡 B (Good)"
            qualityScore >= 60 -> "🟡 C (Fair)"
            qualityScore >= 50 -> "🟠 D (Poor)"
            else -> "🔴 F (Critical)"
        }
        report.appendLine("**Grade**: $grade")
        report.appendLine()

        // Footer
        report.appendLine("---")
        report.appendLine("*Generated by JavaDoc Quality Check*")
        report.appendLine("*Remember: Good JavaDoc explains **what** and **why**, not **how***")

        // Write report
        val outFile = layout.buildDirectory
            .file("reports/javadoc/javadoc-quality-report.md")
            .get()
            .asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(report.toString())

        // Console output
        println("📊 JavaDoc Quality Report Generated!")
        println("📁 Location: ${outFile.relativeTo(project.projectDir)}")
        println("📈 Quality Score: $qualityScore% ($grade)")
        println("🚨 Issues: $criticalCount critical, $warningCount warnings, $infoCount info")
        
        if (criticalCount > 0) {
            println("💀 CRITICAL ISSUES FOUND! Please fix them before proceeding.")
        }
    }
}

data class CommentInfo(
    val file: String,
    val line: Int,
    val length: Int,
    val type: String,
    val content: String
)

private fun scanJavaFiles(dir: File, issues: MutableList<String>, totalComments: MutableList<CommentInfo>) {
    dir.walkTopDown()
        .filter { it.extension == "java" }
        .forEach { file ->
            scanJavaFile(file, issues, totalComments)
        }
}

private fun scanJavaFile(file: File, issues: MutableList<String>, totalComments: MutableList<CommentInfo>) {
    val lines = Files.readAllLines(Paths.get(file.absolutePath))
    val relativePath = file.relativeTo(project.projectDir).path
    
    var inComment = false
    var commentStartLine = 0
    var commentLines = mutableListOf<String>()
    var commentType = ""
    
    lines.forEachIndexed { index, line ->
        val lineNumber = index + 1
        
        when {
            // Start of JavaDoc comment
            line.trim().startsWith("/**") -> {
                inComment = true
                commentStartLine = lineNumber
                commentLines.clear()
                commentType = when {
                    line.contains("class ") || line.contains("interface ") -> "CLASS"
                    line.contains("(") -> "METHOD"
                    else -> "FIELD"
                }
                commentLines.add(line)
            }
            
            // End of JavaDoc comment
            line.trim().startsWith("*/") -> {
                if (inComment) {
                    inComment = false
                    commentLines.add(line)
                    
                    // Analyze comment
                    analyzeComment(relativePath, commentStartLine, commentLines, commentType, issues, totalComments)
                }
            }
            
            // Inside comment
            inComment -> {
                commentLines.add(line)
            }
        }
    }
}

private fun analyzeComment(
    filePath: String,
    startLine: Int,
    lines: List<String>,
    type: String,
    issues: MutableList<String>,
    totalComments: MutableList<CommentInfo>
) {
    // Filter out @param, @return, @throws lines for length calculation
    val contentLines = lines.filter { line ->
        val trimmed = line.trim()
        !trimmed.startsWith("@param") && 
        !trimmed.startsWith("@return") && 
        !trimmed.startsWith("@throws") &&
        !trimmed.startsWith("@see") &&
        !trimmed.startsWith("@deprecated") &&
        !trimmed.startsWith("@since") &&
        !trimmed.startsWith("@author") &&
        !trimmed.startsWith("@version")
    }
    
    val effectiveLength = contentLines.size
    val totalLength = lines.size
    val content = lines.joinToString("\n")
    
    totalComments.add(CommentInfo(filePath, startLine, effectiveLength, type, content))
    
    // Check for excessive detail using effective length (excluding @tags)
    when {
        // Critical: Very long comments (>15 lines)
        effectiveLength > 15 -> {
            issues.add("CRITICAL: $filePath:$startLine - Excessive comment length ($effectiveLength lines, total: $totalLength)")
        }
        
        // Warning: Long comments (10-15 lines)
        effectiveLength > 10 -> {
            issues.add("WARNING: $filePath:$startLine - Comment might be too verbose ($effectiveLength lines, total: $totalLength)")
        }
        
        // Info: Medium comments (6-10 lines) - check content
        effectiveLength > 5 -> {
            if (hasExcessiveDetail(content)) {
                issues.add("INFO: $filePath:$startLine - Comment contains excessive detail ($effectiveLength lines, total: $totalLength)")
            }
        }
    }
    
    // Check for specific anti-patterns
    checkAntiPatterns(filePath, startLine, content, issues)
}

private fun hasExcessiveDetail(content: String): Boolean {
    val antiPatterns = listOf(
        "This method performs",
        "This constructor initializes",
        "The initialization process includes",
        "The validation process checks",
        "This operation is irreversible",
        "The deletion is performed in the correct order",
        "This method delegates",
        "It handles any exceptions",
        "It includes comprehensive validation",
        "It uses a read-only transaction"
    )
    
    return antiPatterns.any { pattern -> content.contains(pattern, ignoreCase = true) }
}

private fun checkAntiPatterns(filePath: String, startLine: Int, content: String, issues: MutableList<String>) {
    val patterns = mapOf(
        "HTML tags" to Pattern.compile("<[^>]+>"),
        "Excessive lists" to Pattern.compile("\\*\\s+<strong>[^<]+</strong>"),
        "Technical implementation details" to Pattern.compile("(validates|initializes|configures|sets up|creates|builds)"),
        "Redundant words" to Pattern.compile("(This method|This constructor|This class|This interface)")
    )
    
    patterns.forEach { (name, pattern) ->
        if (pattern.matcher(content).find()) {
            issues.add("INFO: $filePath:$startLine - Comment contains $name")
        }
    }
}

private fun calculateQualityScore(totalComments: List<CommentInfo>, issues: List<String>): Int {
    if (totalComments.isEmpty()) return 100
    
    val criticalPenalty = issues.count { it.startsWith("CRITICAL") } * 20
    val warningPenalty = issues.count { it.startsWith("WARNING") } * 10
    val infoPenalty = issues.count { it.startsWith("INFO") } * 5
    
    val totalPenalty = criticalPenalty + warningPenalty + infoPenalty
    val maxScore = totalComments.size * 5 // Assume perfect score is 5 points per comment
    
    val score = maxOf(0, maxScore - totalPenalty)
    return ((score.toDouble() / maxScore) * 100).toInt()
}

// Make build depend on JavaDoc report generation
tasks.named("build") {
    dependsOn("generateJavadocReport")
}

// Add task to check task
tasks.named("check") {
    dependsOn("generateJavadocReport")
}
