package com.project.starter.modules.internal

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

internal fun Project.configureKapt() {
    pluginManager.apply("kotlin-kapt")
    extensions.configure(KaptExtension::class.java) {
        it.correctErrorTypes = true
        it.useBuildCache = true
    }
}
