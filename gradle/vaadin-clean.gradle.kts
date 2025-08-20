import org.gradle.api.tasks.Delete

// Additional frontend cleanup commonly used in Vaadin projects
tasks.register<Delete>("cleanFrontend") {
    group = BasePlugin.CLEAN_TASK_NAME
    description = "Deletes Vaadin/Frontend generated artifacts and caches"

    delete(
        // Node/pnpm artifacts
        "node_modules",
        ".pnpm-store",
        "pnpm-lock.yaml",
        "package-lock.json",

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

// Make general clean also run Vaadin's clean and our additional cleanup
tasks.named("clean") {
    // run our extra frontend cleanup as part of clean
    dependsOn("cleanFrontend")
    // run vaadinClean after clean to avoid circular dependency (vaadinClean -> clean)
    finalizedBy("vaadinClean")
}


