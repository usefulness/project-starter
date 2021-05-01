package com.project.starter.modules.internal

import org.gradle.api.Project

internal fun Project.configureRepositories(): Unit = with(repositories) {
    exclusiveContent { content ->
        content.forRepository { google() }
        content.filter {
            val googleLibraries = listOf(
                "com\\.android.*",
                "androidx.*",
                "android.arch.*",
                "com\\.google.*"
            )
            googleLibraries.forEach(it::includeGroupByRegex)
        }
    }
    exclusiveContent { content ->
        content.forRepository {
            maven { it.setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        }
        content.filter { it.includeGroup("org.jacoco") }
    }
    mavenCentral()
}
