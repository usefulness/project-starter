package com.project.starter.config.extensions

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.util.ConfigureUtil

open class RootConfigExtension(
    var javaVersion: JavaVersion = JavaVersion.VERSION_1_8,
    var javaFilesAllowed: Boolean = true
) {

    val quality = QualityPluginConfig()
    val android = AndroidPluginConfig()
    val versioning = VersioningPluginConfig()

    fun qualityPlugin(c: Closure<QualityPluginConfig>) =
        ConfigureUtil.configure(c, quality)

    fun qualityPlugin(action: Action<QualityPluginConfig>) =
        action.execute(quality)

    fun androidPlugin(c: Closure<AndroidPluginConfig>) =
        ConfigureUtil.configure(c, android)

    fun androidPlugin(action: Action<AndroidPluginConfig>) =
        action.execute(android)

    fun versioningPlugin(c: Closure<VersioningPluginConfig>) =
        ConfigureUtil.configure(c, versioning)

    fun versioningPlugin(action: Action<VersioningPluginConfig>) =
        action.execute(versioning)

    fun javaVersion(value: JavaVersion) {
        javaVersion = value
    }

    fun javaFilesAllowed(value: Boolean) {
        javaFilesAllowed = value
    }
}

open class QualityPluginConfig(
    var formatOnCompile: Boolean = false,
    var enabled: Boolean = true
) {
    fun formatOnCompile(value: Boolean) {
        formatOnCompile = value
    }

    fun enabled(value: Boolean) {
        enabled = value
    }
}

open class AndroidPluginConfig(
    var compileSdkVersion: Int = 29,
    var minSdkVersion: Int = 23,
    var targetSdkVersion: Int? = null
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

open class VersioningPluginConfig(
    var enabled: Boolean = true
) {

    fun enabled(value: Boolean) {
        enabled = value
    }
}
