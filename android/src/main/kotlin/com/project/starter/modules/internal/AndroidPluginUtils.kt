package com.project.starter.modules.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasUnitTest
import com.project.starter.config.extensions.RootConfigExtension
import com.project.starter.config.getByType
import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.AndroidExtension
import com.project.starter.modules.tasks.ProjectLintTask.Companion.registerProjectLintTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun CommonExtension.configureAndroidPlugin(rootConfig: RootConfigExtension) {
    defaultConfig.apply {
        compileSdk = rootConfig.android.compileSdkVersion
        minSdk = rootConfig.android.minSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions.apply {
        sourceCompatibility = rootConfig.javaVersion
        targetCompatibility = rootConfig.javaVersion
    }
}

internal inline fun <reified TStarter, reified TAgp> Project.configureAndroidProject()
    where TStarter : AndroidExtension, TAgp : AndroidComponentsExtension<*, *, *> {
    val androidComponents = extensions.getByType(TAgp::class.java)

    val projectLint = registerProjectLintTask()
    val projectTest = registerProjectTestTask()
    tasks.withType(KotlinJvmCompile::class.java).configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(rootConfig.javaVersion.toString()))
    }

    androidComponents.onVariants { variant ->
        val capitalizedName = variant.name.replaceFirstChar(Char::titlecase)
        projectLint.dependsOn("$path:lint$capitalizedName")
        (variant as? HasUnitTest)?.unitTest?.let {
            projectTest.dependsOn("$path:test${it.name.replaceFirstChar(Char::titlecase)}")
        }
    }
}

private fun <T : Task> TaskProvider<out T>.dependsOn(name: String) {
    configure { dependsOn(name) }
}

internal val Project.canAgpCompileKotlin: Boolean
    get() = project.extensions.getByType<AndroidComponentsExtension<*, *, *>>().pluginVersion.major >= 9
