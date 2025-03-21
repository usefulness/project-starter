package com.project.starter.config.extensions

import org.gradle.api.Action
import org.gradle.api.JavaVersion

open class RootConfigExtension(
    var javaVersion: JavaVersion = JavaVersion.VERSION_11,
    var javaFilesAllowed: Boolean = false,
) {

    val quality = QualityPluginConfig()
    val android = AndroidPluginConfig()
    val versioning = VersioningPluginConfig()

    fun qualityPlugin(action: Action<QualityPluginConfig>) = action.execute(quality)

    fun androidPlugin(action: Action<AndroidPluginConfig>) = action.execute(android)

    fun versioningPlugin(action: Action<VersioningPluginConfig>) = action.execute(versioning)

    fun javaVersion(value: JavaVersion) {
        javaVersion = value
    }

    fun javaFilesAllowed(value: Boolean) {
        javaFilesAllowed = value
    }
}

open class QualityPluginConfig(var formatOnCompile: Boolean = false) {
    fun formatOnCompile(value: Boolean) {
        formatOnCompile = value
    }
}

open class AndroidPluginConfig(
    var compileSdkVersion: Int = 35,
    var minSdkVersion: Int = 26,
    var targetSdkVersion: Int? = null,
) {

    fun compileSdkVersion(value: Int) {
        compileSdkVersion = value
    }

    fun minSdkVersion(value: Int) {
        minSdkVersion = value
    }

    fun targetSdkVersion(value: Int) {
        targetSdkVersion = value
    }
}

open class VersioningPluginConfig
