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

    fun qualityPlugin(c: Closure<QualityPluginConfig>) =
        ConfigureUtil.configure(c, quality)

    fun qualityPlugin(action: Action<QualityPluginConfig>) =
        action.execute(quality)

    fun androidPlugin(c: Closure<AndroidPluginConfig>) =
        ConfigureUtil.configure(c, android)

    fun androidPlugin(action: Action<AndroidPluginConfig>) =
        action.execute(android)
}

open class QualityPluginConfig(
    var formatOnCompile: Boolean = false,
    var enabled: Boolean = true
)

open class AndroidPluginConfig(
    var compileSdkVersion: Int = 29,
    var minSdkVersion: Int = 23,
    var targetSdkVersion: Int? = null
)
