import org.gradle.api.tasks.Delete

// Additional frontend cleanup commonly used in Vaadin projects
tasks.register<Delete>("cleanFrontend") {
    group = BasePlugin.CLEAN_TASK_NAME
    description = "Deletes Vaadin/Frontend generated artifacts and caches"

    delete(
        // Vaadin/Vite generated files
        "vite.generated.ts",
        "vite.generated.d.ts",
        "frontend/generated",

        // Historical/target locations (safe to remove if present)
        "target/frontend",
        "target/flow-frontend",

        // Vaadin internal state
        ".vaadin",
    )
}

// Fast clean that preserves node_modules and Vaadin caches
tasks.register("cleanQuick") {
    group = BasePlugin.CLEAN_TASK_NAME
    description = "Fast clean (Gradle build/ only). Preserves node_modules and Vaadin caches"
    dependsOn("clean")
}

// Full clean: Vaadin deep clean + extra frontend cleanup
// Note: vaadinClean already depends on 'clean', so this is a complete cleanup
tasks.register("deepClean") {
    group = BasePlugin.CLEAN_TASK_NAME
    description = "Full clean including Vaadin artifacts and caches"
    dependsOn("clean", "vaadinClean", "cleanFrontend")
}


