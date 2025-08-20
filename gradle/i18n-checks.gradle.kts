import java.io.File
import java.util.Properties
/*
 * I18N keys consistency check: ensure en/ru/es have the same set of keys.
 * Writes a report to build/reports/i18n/missing-keys.md and fails on missing keys.
 */
tasks.register("checkI18nKeys") {
    description = "Validates that all i18n locales have the same keys"
    group = JavaBasePlugin.VERIFICATION_GROUP

    doLast {
        val locales = listOf("en", "ru", "es")
        val baseDir = project.file("src/main/resources/i18n")

        val propsByLocale: Map<String, Properties> =
            locales.associateWith { loc ->
                val p = Properties()
                val f = File(baseDir, "messages_$loc.properties")
                if (!f.exists()) {
                    logger.warn("[i18n] Missing properties file for locale: $loc at ${f.absolutePath}")
                } else {
                    f.inputStream().use { stream -> p.load(stream) }
                }
                p
            }

        val allKeys = mutableSetOf<String>()
        propsByLocale.values.forEach { allKeys.addAll(it.stringPropertyNames()) }

        val report = StringBuilder()
        var missingCount = 0
        for (loc in locales) {
            val present = propsByLocale[loc]?.stringPropertyNames() ?: emptySet()
            val missing = allKeys.minus(present)
            if (missing.isNotEmpty()) {
                missingCount += missing.size
                report.appendLine("## Missing in $loc (${missing.size})")
                missing.sorted().forEach { report.appendLine("- $it") }
                report.appendLine()
            }
        }

        val outFile =
            layout.buildDirectory
                .file("reports/i18n/missing-keys.md")
                .get()
                .asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(if (report.isNotBlank()) report.toString() else "All locales have identical key sets.")

        if (missingCount > 0) {
            throw GradleException("[i18n] Found $missingCount missing keys across locales. See ${outFile.relativeTo(project.projectDir)}")
        } else {
            println("[i18n] No missing keys across locales.")
        }
    }
}

tasks.named("check") {
    dependsOn("checkI18nKeys")
}


