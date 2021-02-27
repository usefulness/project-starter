package com.project.starter.modules.internal

import org.gradle.api.Project

internal fun Project.configureRepositories(): Unit = with(repositories) {
    google { repository ->
        repository.mavenContent { content ->
            val googleLibraries = listOf(
                "com\\.android.*",
                "androidx.*",
                "android.arch.*",
                "com\\.google.*"
            )
            googleLibraries.forEach(content::includeGroupByRegex)
        }
    }
    mavenCentral()
    jcenter { repository ->
        repository.mavenContent { content ->
            content.includeGroup("org.jetbrains.trove4j")
            content.includeGroup("org.jetbrains.kotlinx")
        }
    }
}
