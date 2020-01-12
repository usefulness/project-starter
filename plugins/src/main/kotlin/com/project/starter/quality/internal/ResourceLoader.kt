package com.project.starter.quality.internal

import java.io.File
import java.net.JarURLConnection
import org.gradle.api.Project

internal object Loader

internal fun Project.loadFromResources(path: String): File? {
    val configFile = Loader::class.java.classLoader.getResource(path)

    return when (val jar = configFile?.openConnection()) {
        is JarURLConnection -> resources.text.fromArchiveEntry(jar.jarFileURL, jar.entryName).asFile()
        else -> configFile?.let { File(it.file) }
    }
}
