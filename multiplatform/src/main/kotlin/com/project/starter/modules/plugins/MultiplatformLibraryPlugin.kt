package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.extensions.MultiplatfromLibraryConfigExtension
import com.project.starter.modules.internal.configureMultiplatformCoverage
import com.project.starter.modules.tasks.ProjectCoverageTask.Companion.registerProjectCoverageTask
import com.project.starter.modules.tasks.ProjectTestTask.Companion.registerProjectTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class MultiplatformLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.starter.quality")
        pluginManager.apply(ConfigurationPlugin::class.java)

        extensions.create("projectConfig", MultiplatfromLibraryConfigExtension::class.java)

        registerProjectTestTask {
            it.dependsOn("allTests")
        }
        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(rootConfig.javaVersion.toString()))
        }

        configureMultiplatformCoverage()
        registerProjectCoverageTask { projectCoverage ->
            projectCoverage.dependsOn("jacocoTestReport")
        }
        Unit
    }
}
