package com.project.starter.modules.internal

import org.gradle.api.Project

internal fun Project.configureRepositories(): Unit = with(repositories) {
    runCatching {
        mavenCentral()
        google {
            mavenContent {
                val googleLibraries = listOf(
                    "com\\.android.*",
                    "androidx.*",
                    "android\\.arch.*",
                    "com\\.google\\.android.*",
                    "com\\.google\\.gms",
                    "com\\.google\\.test.*",
                    "com\\.google\\.ads.*",
                    "com\\.google\\.ar.*",
                    "com\\.google\\.mlkit.*",
                    "com\\.google\\.devtools.*",
                    "com\\.google\\.assistant.*",
                    "com\\.google\\.oboe.*",
                    "com\\.google\\.prefab.*",
                )
                googleLibraries.forEach(::includeGroupByRegex)
            }
        }
    }
        .onFailure { logger.info("Build was configured using FAIL_ON_PROJECT_REPOS option") }
}
