package com.project.starter.modules.internal

import org.gradle.api.Project

internal fun Project.configureRepositories(): Unit = with(repositories) {
    exclusiveContent { content ->
        content.forRepository { google() }
        content.filter {
            val googleLibraries = listOf(
                "com\\.android.*",
                "androidx\\..*",
                "android\\.arch\\..*",
                "com\\.google\\.android\\..*",
                "com\\.google\\.gms",
                "com\\.google\\.test",
                "com\\.google\\.ads\\..*",
                "com\\.google\\.ar\\..*",
                "com\\.google\\.mlkit.*",
                "com\\.google\\.devtools.*",
                "com\\.google\\.assistant.*",
                "com\\.google\\.oboe.*",
                "com\\.google\\.prefab.*",
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
