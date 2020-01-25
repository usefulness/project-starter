package com.project.starter.quality.internal

import java.io.File
import java.net.JarURLConnection
import org.gradle.api.Project

private object ResourceLoader

internal fun Project.loadFromResources(path: String): File? {
    val configFile = ResourceLoader::class.java.classLoader.getResource(path)

    @Suppress("UseIfInsteadOfWhen")
    return when (val jar = configFile?.openConnection()) {
        is JarURLConnection -> resources.text.fromArchiveEntry(jar.jarFileURL, jar.entryName).asFile()
        else -> configFile?.let { File(it.file) }
    }
}
