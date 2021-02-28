package com.project.starter.quality.internal

import java.io.File
import java.net.JarURLConnection
import org.gradle.api.Project
import org.gradle.api.provider.Provider

private object ResourceLoader

internal fun Project.loadFromResources(path: String): Provider<File?> {
    val configFile = ResourceLoader::class.java.classLoader.getResource(path)

    return provider {
        @Suppress("UseIfInsteadOfWhen")
        when (val jar = configFile?.openConnection()) {
            is JarURLConnection -> resources.text.fromArchiveEntry(jar.jarFileURL, jar.entryName).asFile()
            else -> configFile?.let { File(it.file) }
        }?.also {
            logger.info("Loaded config: $it")
        }
    }
}
